package com.example.futsal.activities


import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.futsal.Booking.futsalbook
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_detail.*
import com.google.firebase.database.DataSnapshot
import com.example.futsal.R
import com.example.futsal.modal.Comment
import com.example.futsal.modal.FutsalModel
import com.example.futsal.modal.User
import kotlinx.android.synthetic.main.item_comment.view.*
import android.widget.RatingBar
import com.example.futsal.modal.RatingModel


private  var commentText:EditText? = null
private var commenttxt:String?=null
private lateinit var ratingref: DatabaseReference
private lateinit var postReference: DatabaseReference
private lateinit var commentsReference: DatabaseReference
private lateinit var futref: DatabaseReference
val ref = FirebaseDatabase.getInstance().reference

class DetailActivity : AppCompatActivity(), RatingBar.OnRatingBarChangeListener {


    private var ratingbar: RatingBar? = null
    private val TAG = "DetailActivity"
    private lateinit var postKey: String
    private var postListener: ValueEventListener? = null
    private var adapter: CommentAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        commentText=findViewById<EditText>(R.id.fieldCommentText)
        ratingbar = findViewById<RatingBar>(R.id.ratingBar)

        postKey = intent.getStringExtra("firebase_id")

        button_book.setOnClickListener {
            if (FirebaseAuth.getInstance().currentUser != null) {
                val intent = Intent(this, futsalbook::class.java)
                intent.putExtra("fid", postKey)
                startActivity(intent)
            } else {
                Toast.makeText(this, "login in to book", Toast.LENGTH_SHORT).show()
            }
        }


            // Initialize Database
            postReference = FirebaseDatabase.getInstance().reference
                .child("futsal")
             futref = FirebaseDatabase.getInstance().reference
            .child("futsal").child(postKey)
            commentsReference = FirebaseDatabase.getInstance().reference
                .child("comment").child(postKey)
            ratingref= FirebaseDatabase.getInstance().reference
                .child("ratings").child(postKey)

            // Initialize Views
            buttonPostComment.setOnClickListener{
                postComment()
            }
            recyclerPostComments.layoutManager = LinearLayoutManager(this)
        }

        override fun onStart() {
            super.onStart()

            // Add value event listener to the post
            // [START post_value_event_listener]
            val postListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Get Post object and use the values to update the UI
                    val post = dataSnapshot.getValue(FutsalModel::class.java)
                    // [START_EXCLUDE]
                    val bundle: Bundle? = intent.extras
                    val Image = bundle!!.getString("firebase_image")
                    val Name = bundle.getString("firebase_title")
                    val Desc = bundle.getString("firebase_desc")
                    post?.let {
                        txttitle.text=Name
                        txtdescription.text=Desc
                        Picasso.with(this@DetailActivity).load(Image).into(imgview)
                                           }

                    // [END_EXCLUDE]
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Post failed, log a message
                    Log.w(TAG, "detail:onCancelled", databaseError.toException())
                    // [START_EXCLUDE]
                    Toast.makeText(baseContext, "Failed to load detail.",
                        Toast.LENGTH_SHORT).show()
                    // [END_EXCLUDE]
                }
            }
            postReference.addValueEventListener(postListener)
            // [END post_value_event_listener]

            // Keep copy of post listener so we can remove it when app stops
            this.postListener = postListener

            // Listen for comments
            adapter = CommentAdapter(this, commentsReference)
            recyclerPostComments.adapter = adapter
            testRatingBar()
        }

    private fun testRatingBar() {
        val userid= FirebaseAuth.getInstance().currentUser!!.uid
        ratingref.child(userid).child("stars").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.value != null) {
                    val rating = java.lang.Float.parseFloat(p0.value.toString())
                    ratingBar.rating = rating
                }
        }})
        ratingBar.onRatingBarChangeListener=this
        }
    override fun onRatingChanged(ratingBar: RatingBar?, rating: Float, fromUser: Boolean) {
        val userid= FirebaseAuth.getInstance().currentUser!!.uid
        val bundle: Bundle? = intent.extras
        val Name = bundle?.getString("firebase_title")
        val passrating=RatingModel(rating.toString(),Name,userid)
        ratingref.child(userid).setValue(passrating)
    }


    public override fun onStop() {
            super.onStop()

            // Remove post value event listener
            postListener?.let {
                postReference.removeEventListener(it)
            }

            // Clean up comments listener
            adapter?.cleanupListener()
        }

        }

        private fun postComment() {

            val userid= FirebaseAuth.getInstance().currentUser!!.uid
            FirebaseDatabase.getInstance().reference.child("Users").child(userid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        // Get user information
                        val user = dataSnapshot.getValue(User::class.java)
                        if (user == null) {
                            return
                        }
                        var username=dataSnapshot.child("Name").value as String

                      futref.addValueEventListener(object :ValueEventListener{
                            override fun onCancelled(p0: DatabaseError) {
                                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                            }

                            override fun onDataChange(p0: DataSnapshot) {
                                Log.d("key", "onChild:" + p0.key)
                                val post = p0.getValue(FutsalModel::class.java)
                                var fname=post!!.Name
                                Log.d("name", "ondata:" + fname)
                                commenttxt = commentText?.text.toString()
                                val comment = Comment(username,fname, commenttxt)

                                // Push the comment, it will appear in the list
                                commentsReference.push().setValue(comment)

                                // Clear the field
                                commenttxt = null

                            }

                        })


                                    }


                    override fun onCancelled(databaseError: DatabaseError) {
                    }})
        }

private class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            fun bind(comment: Comment) {
                itemView.commentuser.text = comment.uid
                itemView.commentBody.text = comment.text
            }
        }

        private class CommentAdapter(
            private val context: Context,
            private val databaseReference: DatabaseReference
        ) : RecyclerView.Adapter<CommentViewHolder>() {

            private val childEventListener: ChildEventListener?

            private val commentIds = ArrayList<String>()
            private val comments = ArrayList<Comment>()

            init {

                // Create child event listener
                // [START child_event_listener_recycler]
                val childEventListener = object : ChildEventListener {
                    override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                        Log.d(TAG, "onChildAdded:" + dataSnapshot.key!!)

                        // A new comment has been added, add it to the displayed list
                        val comment = dataSnapshot.getValue(Comment::class.java)

                        // [START_EXCLUDE]
                        // Update RecyclerView
                        commentIds.add(dataSnapshot.key!!)
                        comments.add(comment!!)
                        notifyItemInserted(comments.size - 1)
                        // [END_EXCLUDE]
                    }

                    override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                        Log.d(TAG, "onChildChanged: ${dataSnapshot.key}")

                        // A comment has changed, use the key to determine if we are displaying this
                        // comment and if so displayed the changed comment.
                        val newComment = dataSnapshot.getValue(Comment::class.java)
                        val commentKey = dataSnapshot.key

                        // [START_EXCLUDE]
                        val commentIndex = commentIds.indexOf(commentKey)
                        if (commentIndex > -1 && newComment != null) {
                            // Replace with the new data
                            comments[commentIndex] = newComment

                            // Update the RecyclerView
                            notifyItemChanged(commentIndex)
                        } else {
                            Log.w(TAG, "onChildChanged:unknown_child: $commentKey")
                        }
                        // [END_EXCLUDE]
                    }

                    override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                        Log.d(TAG, "onChildRemoved:" + dataSnapshot.key!!)

                        // A comment has changed, use the key to determine if we are displaying this
                        // comment and if so remove it.
                        val commentKey = dataSnapshot.key

                        // [START_EXCLUDE]
                        val commentIndex = commentIds.indexOf(commentKey)
                        if (commentIndex > -1) {
                            // Remove data from the list
                            commentIds.removeAt(commentIndex)
                            comments.removeAt(commentIndex)

                            // Update the RecyclerView
                            notifyItemRemoved(commentIndex)
                        } else {
                            Log.w(TAG, "onChildRemoved:unknown_child:" + commentKey!!)
                        }
                        // [END_EXCLUDE]
                    }

                    override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                        Log.d(TAG, "onChildMoved:" + dataSnapshot.key!!)

                        // A comment has changed position, use the key to determine if we are
                        // displaying this comment and if so move it.
                        val movedComment = dataSnapshot.getValue(Comment::class.java)
                        val commentKey = dataSnapshot.key

                        // ...
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.w(TAG, "postComments:onCancelled", databaseError.toException())
                        Toast.makeText(context, "Failed to load comments.",
                            Toast.LENGTH_SHORT).show()
                    }
                }
                databaseReference.addChildEventListener(childEventListener)
                // [END child_event_listener_recycler]

                // Store reference to listener so it can be removed on app stop
                this.childEventListener = childEventListener
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
                val inflater = LayoutInflater.from(context)
                val view = inflater.inflate(R.layout.item_comment, parent, false)
                return CommentViewHolder(view)
            }

            override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
                holder.bind(comments[position])
            }

            override fun getItemCount(): Int = comments.size

            fun cleanupListener() {
                childEventListener?.let {
                    databaseReference.removeEventListener(it)
                }
            }

        }


