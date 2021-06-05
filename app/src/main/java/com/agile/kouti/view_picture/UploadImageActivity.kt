package com.agile.kouti.view_picture

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.Point
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.hardware.camera2.params.StreamConfigurationMap
import android.media.Image
import android.media.ImageReader
import android.net.Uri
import android.os.*
import android.text.TextUtils
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView.SurfaceTextureListener
import android.view.View
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import com.agile.kouti.KoutiBaseActivity
import com.agile.kouti.R
import com.agile.kouti.db.FirebaseDbClient
import com.agile.kouti.db.picture.PictureList
import com.agile.kouti.utils.CameraUtils
import com.agile.kouti.utils.Const
import com.agile.kouti.utils.Preferences
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_upload_image.*
import kotlinx.android.synthetic.main.toolbar.*
import timber.log.Timber
import java.io.*
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class UploadImageActivity : KoutiBaseActivity(),View.OnClickListener, KoutiBaseActivity.OnImageSelectedListener {

    private lateinit var manager: CameraManager
    private var imagePath: Uri? = null
    private var imageUrl: String? = ""

    private var pictureId: String? = ""
    var pictureObj: PictureList? = null

    private var firebaseStore: FirebaseStorage? = null
    private var storageReference: StorageReference? = null

    private var lastImageCaptureFrom = LastImageCapture.NONE


    companion object {
        private val ORIENTATIONS: SparseIntArray = SparseIntArray()

        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }

        private const val REQUEST_CAMERA_PERMISSION = 200

    }

    private var mMaxPreviewWidth = 0
    private var mMaxPreviewHeight = 0

    private var cameraId: String? = null
    private var cameraDevice: CameraDevice? = null
    private var cameraCaptureSessions: CameraCaptureSession? = null
    private var captureRequestBuilder: CaptureRequest.Builder? = null
    private var imageDimension: Size? = null
    private var imageReader: ImageReader? = null

    private val file: File = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            .toString() + "/" + System.currentTimeMillis() + ".jpg"
    )

    private var captureByteArray: ByteArray? = null

    private var mBackgroundHandler: Handler? = null
    private var mBackgroundThread: HandlerThread? = null

    private val mCameraOpenCloseLock: Semaphore = Semaphore(1)

    private var mSensorOrientation = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_image)

        init()
    }

    private fun init() {
        setToolbar()
        initComponent()
        initViews()
        initListeners()
    }

    private fun setToolbar() {
        ivEdit.setImageResource(R.drawable.list)
        ivEdit.visibility = View.VISIBLE
        ivEdit.setOnClickListener {
            redirectToList()
        }
    }

    /**
     * Initialization of components.
     */
    private fun initComponent() {
        firebaseStore = FirebaseStorage.getInstance()
        storageReference = FirebaseStorage.getInstance().reference

        mMaxPreviewWidth = textureView.width
        mMaxPreviewHeight = textureView.height
    }

    /**
     * Initialization of views.
     */
    private fun initViews() {
        tvToolbarTitle.text = getString(R.string.toolbar_title_take_picture)
    }

    /**
     * Initialization of listeners.
     */
    private fun initListeners() {
        ivBack.setOnClickListener(this)
        llTakePic1.setOnClickListener(this)
        llTakePic2.setOnClickListener(this)
        btnUpload.setOnClickListener(this)
        btnCapture.setOnClickListener(this)
        btnConfirm.setOnClickListener(this)
        textureView.surfaceTextureListener = textureListener
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.ivBack -> {
                finish()
            }

            R.id.llTakePic1, R.id.llTakePic2 -> {
                hideKeyBoard(v)
                setListener(this)

                if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)) {
                    if (checkPermission()) {
                        openGallery(Const.KEYS.REFERENCE)
                    }
                } else {
                    openGallery(Const.KEYS.REFERENCE)
                }
            }

            R.id.btnUpload -> {
//                if (imagePath == null) {
//                    showError("Please select image")
//                    return
//                }
//                uploadImage()

            }

            R.id.btnCapture -> {
                takePicture()
            }

            R.id.btnConfirm -> {

                if (lastImageCaptureFrom == LastImageCapture.CAMERA) {
                    save(captureByteArray!!)
                    imagePath = Uri.fromFile(file)
                }

                if (imagePath == null) {
                    showError("Please select image")
                    return
                }
                uploadImage()
            }
        }

    }

    private var textureListener: SurfaceTextureListener = object : SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            openCamera(width, height)
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
            if (null != textureView || null == imageDimension) {
                textureView.setTransform(
                    CameraUtils.configureTransform(
                        width,
                        height,
                        imageDimension!!,
                        this@UploadImageActivity
                    )
                )
            }
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            return false
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
    }

    private var stateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            Log.e("tag", "onOpened")
            mCameraOpenCloseLock.release()
            cameraDevice = camera
            createCameraPreview()
        }

        override fun onDisconnected(camera: CameraDevice) {
            mCameraOpenCloseLock.release()
            cameraDevice?.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            mCameraOpenCloseLock.release()
            cameraDevice?.close()
            cameraDevice = null
        }
    }

    private class CompareSizesByArea : Comparator<Size> {
        override fun compare(lhs: Size, rhs: Size): Int {
            return java.lang.Long.signum(
                lhs.width.toLong() * lhs.height -
                        rhs.width.toLong() * rhs.height
            )
        }

    }

    private fun openCamera(width: Int, height: Int) {
        manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
//            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
//                throw RuntimeException("Time out waiting to lock camera opening.")
//            }
            cameraId = manager.cameraIdList.get(0)
            val characteristics: CameraCharacteristics = manager.getCameraCharacteristics(cameraId!!)
            val map: StreamConfigurationMap =
                characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!

            val sizeList = ArrayList<Size>()
            sizeList.addAll(map.getOutputSizes(ImageFormat.JPEG))

            val largest: Size = Collections.max(
                sizeList, CompareSizesByArea()
            )
            val displayRotation = windowManager.defaultDisplay.rotation
            mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!!
            var swappedDimensions = false
            when (displayRotation) {
                Surface.ROTATION_0, Surface.ROTATION_180 -> if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                    swappedDimensions = true
                }
                Surface.ROTATION_90, Surface.ROTATION_270 -> if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                    swappedDimensions = true
                }
                else -> Log.e("tag", "Display rotation is invalid: $displayRotation")
            }
            val displaySize = Point()
            windowManager.defaultDisplay.getSize(displaySize)
            var rotatedPreviewWidth = width
            var rotatedPreviewHeight = height
            var maxPreviewWidth: Int = displaySize.x
            var maxPreviewHeight: Int = displaySize.y
            if (swappedDimensions) {
                rotatedPreviewWidth = height
                rotatedPreviewHeight = width
                maxPreviewWidth = displaySize.y
                maxPreviewHeight = displaySize.x
            }
            if (maxPreviewWidth > mMaxPreviewWidth) {
                maxPreviewWidth = mMaxPreviewWidth
            }
            if (maxPreviewHeight > mMaxPreviewHeight) {
                maxPreviewHeight = mMaxPreviewHeight
            }
            imageDimension = CameraUtils.chooseOptimalSize(
                map.getOutputSizes(SurfaceTexture::class.java),
                rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                maxPreviewHeight, largest
            )
            val orientation = resources.configuration.orientation
//            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                textureView.setAspectRatio(
//                    imageDimension!!.width, imageDimension!!.height
//                )
//            } else {
//                textureView.setAspectRatio(
//                    imageDimension!!.height, imageDimension!!.width
//                )
//            }
            if (null != textureView || null == imageDimension) {
                textureView.setTransform(
                    CameraUtils.configureTransform(
                        width,
                        height,
                        imageDimension!!,
                        this@UploadImageActivity
                    )
                )
            }
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@UploadImageActivity,
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    REQUEST_CAMERA_PERMISSION
                )
                return
            }
            manager.openCamera(cameraId!!, stateCallback, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        Log.e("tag", "openCamera X")
    }

    private fun createCameraPreview() {
        try {
            val texture = textureView.surfaceTexture!!
            texture.setDefaultBufferSize(imageDimension!!.width, imageDimension!!.height)
            val surface = Surface(texture)
            captureRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder?.set(
                CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
            )
            captureRequestBuilder?.addTarget(surface)
            cameraDevice?.createCaptureSession(Arrays.asList(surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(@NonNull cameraCaptureSession: CameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession
                    updatePreview()
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Toast.makeText(this@UploadImageActivity, "Configuration change", Toast.LENGTH_SHORT)
                        .show()
                }
            }, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun updatePreview() {
        if (null == cameraDevice) {
            Log.e("tag", "updatePreview error, return")
        }
        captureRequestBuilder?.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
        try {
            cameraCaptureSessions?.setRepeatingRequest(
                captureRequestBuilder!!.build(),
                null,
                mBackgroundHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("Camera Background")
        mBackgroundThread?.start()
        mBackgroundHandler = Handler(mBackgroundThread?.getLooper())
    }

    private fun stopBackgroundThread() {
        mBackgroundThread?.quitSafely()
        try {
            mBackgroundThread?.join()
            mBackgroundThread = null
            mBackgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun closeCamera() {
        try {
            mCameraOpenCloseLock.acquire()
            if (null != cameraCaptureSessions) {
                cameraCaptureSessions?.close()
                cameraCaptureSessions = null
            }
            if (null != cameraDevice) {
                cameraDevice?.close()
                cameraDevice = null
            }
            if (null != imageReader) {
                imageReader?.close()
                imageReader = null
            }
        } catch (e: InterruptedException) {
            throw java.lang.RuntimeException("Interrupted while trying to lock camera closing.")
        } finally {
            mCameraOpenCloseLock.release()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(
                    this@UploadImageActivity,
                    "Sorry!!!, you can't use this app without granting permission",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            } else {
                startBackgroundThread()
                if (textureView.isAvailable) {
                    openCamera(textureView.width, textureView.height)
                } else {
                    textureView.surfaceTextureListener = textureListener
                }
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        if (requestCode == REQUEST_CAMERA_PERMISSION) {
//            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
//                // close the app
//                Toast.makeText(
//                    this@UploadImageActivity,
//                    "Sorry!!!, you can't use this app without granting permission",
//                    Toast.LENGTH_LONG
//                ).show()
//                finish()
//            } else {
//                manager.openCamera(cameraId!!, stateCallback, null)
//            }
//        }
//    }

    override fun onResume() {
        super.onResume()
        Log.e("tag", "onResume")
        if (captureByteArray == null) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@UploadImageActivity,
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    REQUEST_CAMERA_PERMISSION
                )
                return
            }

            startBackgroundThread()
            if (textureView.isAvailable) {
                openCamera(textureView.width, textureView.height)
            } else {
                textureView.surfaceTextureListener = textureListener
            }
        }
    }

    override fun onPause() {
        Log.e("tag", "onPause")
        if (captureByteArray == null) {
            closeCamera()
            stopBackgroundThread()
        }

        super.onPause()
    }

    private fun takePicture() {
        if (null == cameraDevice) {
            Log.e("tag", "cameraDevice is null")
            return
        }
        val manager = getSystemService(CAMERA_SERVICE) as CameraManager
        try {
            val characteristics = manager.getCameraCharacteristics(cameraDevice!!.getId())
            var jpegSizes: Array<Size>? = null
            if (characteristics != null) {
                jpegSizes =
                    characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
                        .getOutputSizes(ImageFormat.JPEG)
            }
            var width = 640
            var height = 480
            if (jpegSizes != null && 0 < jpegSizes.size) {
                width = jpegSizes[0].width
                height = jpegSizes[0].height
            }
            val reader: ImageReader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1)
            val outputSurfaces: ArrayList<Surface> = ArrayList(2)
            outputSurfaces.add(reader.getSurface())
            outputSurfaces.add(Surface(textureView.surfaceTexture))
            val captureBuilder: CaptureRequest.Builder =
                cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder.addTarget(reader.getSurface())
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
            // Orientation
            val rotation = windowManager.defaultDisplay.rotation
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation))
            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler)
            val captureListener: CameraCaptureSession.CaptureCallback = object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult
                ) {
                    super.onCaptureCompleted(session, request, result)

                    //Toast.makeText(this@UploadImageActivity, "Saved:$file", Toast.LENGTH_SHORT).show()
                    //createCameraPreview()
                }
            }
            cameraDevice?.createCaptureSession(outputSurfaces, object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler)
                    } catch (e: CameraAccessException) {
                        e.printStackTrace()
                    }
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {}
            }, mBackgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private var readerListener: ImageReader.OnImageAvailableListener = object : ImageReader.OnImageAvailableListener {
        override fun onImageAvailable(reader: ImageReader) {
            var image: Image? = null
            try {
                image = reader.acquireLatestImage()
                val buffer: ByteBuffer = image.getPlanes().get(0).getBuffer()
                val bytes = ByteArray(buffer.capacity())
                buffer.get(bytes)
                captureByteArray = bytes

                runOnUiThread {
                    lastImageCaptureFrom = LastImageCapture.CAMERA
                    setImagePreview()
                }
                // closeCamera()
                // stopBackgroundThread()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                if (image != null) {
                    image.close()
                }
            }
        }
    }

    private fun setImagePreview() {

        val layoutParams = clPreview.layoutParams
        layoutParams.width = textureView.width
        layoutParams.height = textureView.height
        clPreview.layoutParams = layoutParams

        Glide.with(this@UploadImageActivity)
            .load(if(lastImageCaptureFrom == LastImageCapture.CAMERA) captureByteArray else imagePath)
            .placeholder(R.drawable.upload_document)
            .into(ivImage)
        slPreview.visibility = View.VISIBLE
        llCamera.visibility = View.GONE
    }

    @Throws(IOException::class)
    private fun save(bytes: ByteArray) {
        var output: OutputStream? = null
        try {
            output = FileOutputStream(file)
            output.write(bytes)
        } finally {
            output?.close()
        }
    }

    private fun uploadImage() {

        showProgressDialog()
        if (imagePath != null) {
            val ref = storageReference?.child(
                Const.ImageType.PICTURE + "/" + UUID.randomUUID().toString()
            )
            val uploadTask = ref?.putFile(imagePath!!)
            val urlTask =
                uploadTask?.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    return@Continuation ref.downloadUrl
                })?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        imageUrl = task.result.toString()
                        Timber.e("imageUrl -- " + imageUrl)

                        saveImageInDB()

                    } else {
                        hideProgressDialog()
                        showError(getString(R.string.error_image_upload_failed))
                    }
                }?.addOnFailureListener {
                    hideProgressDialog()
                    showError(getString(R.string.error_failed_try_again))
                }
        } else {
            hideProgressDialog()
            Toast.makeText(this, getString(R.string.error_upload_image), Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveImageInDB() {
        showProgressDialog()
        pictureId = FirebaseDatabase.getInstance().getReference(Const.TableName.PICTURE)
            .push().key.toString()
        var userId = Preferences.getPreference(this, Const.SharedPrefs.USER_ID)
        pictureObj = PictureList(pictureId, imageUrl, userId)
        var firebaseDbClient = FirebaseDbClient()
        if (!TextUtils.isEmpty(pictureId)) {
            firebaseDbClient.picture.child(pictureId.toString()).setValue(pictureObj)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        hideProgressDialog()
                        showAlert("Success")
                        //finish()

                        redirectToList()
                    }
                }
        }
    }

    private fun redirectToList() {

        captureByteArray = null
        imagePath = null
        imageUrl = ""
        pictureId = ""
        pictureObj = null

        lastImageCaptureFrom = LastImageCapture.NONE

        Handler().postDelayed(Runnable {
            slPreview.visibility = View.GONE
            llCamera.visibility = View.VISIBLE
        }, 1000)


        hideProgressDialog()

        val intent = Intent(this, ViewPictureActivity::class.java)
        startActivity(intent)
    }

    override fun onGalleryImage(selectedGalleryImage: Uri?, imageType: String?) {
        imagePath = selectedGalleryImage
        lastImageCaptureFrom = LastImageCapture.GALLERY

        captureByteArray = ByteArray(3)
        //   closeCamera()
        //  stopBackgroundThread()

        setImagePreview()

    }

    override fun onCameraImage(imageUri: Uri?, imageType: String?) {
        imagePath = imageUri
        Glide.with(ivImage!!)
            .load(imageUri)
            .centerCrop()
            .placeholder(R.drawable.upload_document)
            .into(ivImage)
    }

    enum class LastImageCapture {
        NONE,
        CAMERA,
        GALLERY
    }

}