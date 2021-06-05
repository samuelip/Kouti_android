package com.agile.kouti.view_picture

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.agile.kouti.KoutiBaseActivity
import com.agile.kouti.R
import com.agile.kouti.db.FirebaseDbClient
import com.agile.kouti.db.picture.PictureList
import com.agile.kouti.utils.Const
import com.agile.kouti.utils.Preferences
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_view_picture.*
import kotlinx.android.synthetic.main.search_layout.ivBack
import kotlinx.android.synthetic.main.toolbar.*

class ViewPictureActivity : KoutiBaseActivity(), View.OnClickListener, OnPictureClickListener {

    private val pictureList = ArrayList<PictureList>()
    private val pictureListAdapter = PictureAdapter(this, pictureList, this)

    var userId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_picture)
        initData()
    }

    private fun initData() {
        ivBack.setOnClickListener(this)
        llUploadPic.setOnClickListener(this)

        userId = Preferences.getPreference(this, Const.SharedPrefs.USER_ID)

        tvToolbarTitle.text = getString(R.string.toolbar_title_view_picture)

        setPictureListAdapter()

        getPictureList()
    }


    private fun setPictureListAdapter() {
//        val layoutManager = LinearLayoutManager(this@ViewPictureActivity, LinearLayoutManager.HORIZONTAL, false)
//        rvPicture.layoutManager = layoutManager
//        rvPicture.adapter = pictureListAdapter

        rvPicture.layoutManager = GridLayoutManager(this, 2)
        rvPicture.adapter = pictureListAdapter

    }

    private fun getPictureList() {

        if (TextUtils.isEmpty(userId))
            return
//        showProgressDialog()
//        val databaseReference= FirebaseDatabase.getInstance().getReference(Const.TableName.PICTURE)
//        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    for (data in dataSnapshot.children) {
//                        val l: PictureList? = data.getValue<PictureList>(PictureList::class.java)
//                        if (l != null) {
//                            pictureList.add(l)
//                        }
//                    }
//                    pictureListAdapter.notifyDataSetChanged()
//                    hideProgressDialog()
//                }else{
//                    hideProgressDialog()
//                    showError("No Data Found")
//                }
//            }
//            override fun onCancelled(error: DatabaseError) {
//                hideProgressDialog()
//            }
//        })

        showProgressDialog()
        val mFirebaseClient = FirebaseDbClient()
        val recentPostsQuery: Query = mFirebaseClient.picture.orderByChild(Const.KEYS.USER_ID).equalTo(userId)
        recentPostsQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (data in dataSnapshot.children) {
                        val l: PictureList? = data.getValue<PictureList>(PictureList::class.java)
                        if (l != null) {
                            pictureList.add(l)
                        }
                    }
                    pictureListAdapter.notifyDataSetChanged()
                    hideProgressDialog()
                } else {
                    hideProgressDialog()
                    showAlert("No image found")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) { hideProgressDialog()}
        })


    }

    override fun onClick(v: View?) {

        when (v?.id) {
            R.id.ivBack -> {
                finish()
            }

            R.id.llUploadPic -> {
                val intent = Intent(this, ViewPictureActivity::class.java)
                startActivity(intent)
            }
        }

    }

    override fun onPictureItemClick(list: PictureList) {


    }
}