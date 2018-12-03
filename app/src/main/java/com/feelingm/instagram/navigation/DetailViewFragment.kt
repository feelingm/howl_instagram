package com.feelingm.instagram.navigation

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.company.howl.howlstagram.model.FollowDto
import com.feelingm.instagram.MainActivity
import com.feelingm.instagram.R
import com.feelingm.instagram.model.ContentDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_detail.*
import kotlinx.android.synthetic.main.item_detail.view.*

class DetailViewFragment : Fragment() {

    lateinit var user: FirebaseUser

    lateinit var firestore: FirebaseFirestore

    lateinit var imagesSnapshot: ListenerRegistration

    lateinit var mainView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        user = FirebaseAuth.getInstance().currentUser!!
        firestore = FirebaseFirestore.getInstance()

        mainView = inflater.inflate(R.layout.fragment_detail, container, false)

        detailviewfragment_recyclerview.layoutManager = LinearLayoutManager(activity)
        detailviewfragment_recyclerview.adapter = DetailRecyclerViewAdapter()

        return mainView
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).progress_bar.visibility = View.INVISIBLE
    }

    inner class DetailRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        val contentDtos: MutableList<ContentDto> = mutableListOf()
        val contentUidList: MutableList<String> = mutableListOf()

        init {
            val uid = FirebaseAuth.getInstance().currentUser?.uid

            firestore.collection("users").document(uid!!).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userDto = task.result?.toObject(FollowDto::class.java)
                    if (userDto?.followings != null) {
                        getContents(userDto.followings)
                    }
                }
            }
        }

        private fun getContents(followers: MutableMap<String, Boolean>) {
            imagesSnapshot = firestore.collection("images")
                    .orderBy("timestamp").addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                        contentDtos.clear()
                        contentUidList.clear()

                        if (querySnapshot == null) {
                            return@addSnapshotListener
                        }

                        for (snapshot in querySnapshot.documents) {
                            snapshot.toObject(ContentDto::class.java)?.let { item ->
                                if (followers.keys.contains(item.uid)) {
                                    contentDtos.add(item)
                                    contentUidList.add(snapshot.id)
                                }
                            }
                        }

                        notifyDataSetChanged()
                    }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return CustomViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.item_detail, parent, false))

        }

        override fun getItemCount(): Int {
            return contentDtos.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val viewHolder = holder.itemView

            firestore.collection("profileImages").document(contentDtos[position].uid!!).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val url = task.result?.get("image")
                    Glide.with(holder.itemView.context)
                            .load(url)
                            .apply(RequestOptions().circleCrop())
                            .into(viewHolder.detailviewitem_profile_image)
                }
            }


            viewHolder.detailviewitem_profile_image.setOnClickListener {

                val bundle = Bundle().apply {
                    putString("destinationUid", contentDtos[position].uid)
                    putString("userId", contentDtos[position].userId)
                }
                val fragment = UserFragment().apply {
                    arguments = bundle
                }

                activity!!.supportFragmentManager.beginTransaction()
                        .replace(R.id.main_content, fragment)
                        .commit()
            }

            viewHolder.detailviewitem_profile_textview.text = contentDtos[position].userId

            Glide.with(holder.itemView.context)
                    .load(contentDtos[position].imageUrl)
                    .into(viewHolder.detailviewitem_imageview_content)

            viewHolder.detailviewitem_explain_textview.text = contentDtos[position].explain

            viewHolder.detailviewitem_favorite_imageview.setOnClickListener { favoriteEvent(position) }

            if (contentDtos[position].favorites.containsKey(FirebaseAuth.getInstance().currentUser!!.uid)) {
                viewHolder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite)
            } else {
                viewHolder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite_border)
            }

            viewHolder.detailviewitem_favoritecounter_textview.text = "Like " + contentDtos[position].favoriteCount
        }

        private fun favoriteEvent(position: Int) {

            var tsDoc = firestore.collection("images").document(contentUidList[position])

            firestore.runTransaction { transaction ->
                val uid = FirebaseAuth.getInstance().currentUser!!.uid
                val contentDto = transaction.get(tsDoc).toObject(ContentDto::class.java)

                if (contentDto!!.favorites.containsKey(uid)) {
                    contentDto.favoriteCount = contentDto.favoriteCount - 1
                    contentDto.favorites -= uid
                } else {
                    contentDto.favoriteCount = contentDto.favoriteCount + 1
                    contentDto.favorites += uid to true
                }

                transaction.set(tsDoc, contentDto)
            }

        }
    }

    inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}