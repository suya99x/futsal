package com.example.futsal.activities

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.futsal.R
import com.example.futsal.modal.FutsalModel
import com.example.futsal.modal.Message
import com.example.futsal.modal.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.item_comment.view.*
import kotlinx.android.synthetic.main.item_message.view.*

private val TAG = "MessageActivity"
private  var msgText: EditText? = null
private var msgtxt:String?=null
private lateinit var messageReference: DatabaseReference


class ChatActivity:  AppCompatActivity() {
    private var adapter: MessageAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        msgText=findViewById<EditText>(R.id.fieldmsg)

        buttonsendmsg.setOnClickListener{
            sendmessage()
        }

        messageReference = FirebaseDatabase.getInstance().reference
            .child("messages")
        chatrecylerview.layoutManager = LinearLayoutManager(this)
    }

override fun onStart() {
    super.onStart()
        // Listen for comments
    adapter = MessageAdapter(this, messageReference)
    chatrecylerview.adapter = adapter
}

public override fun onStop() {
    super.onStop()
    adapter?.cleanupListener()
}

}

private fun sendmessage() {
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
                // Create new comment object
                msgtxt= msgText?.text.toString()
                val message = Message(userid,username, msgtxt)

                // Push the comment, it will appear in the list
                messageReference.push().setValue(message)

                // Clear the field
                msgtxt=null
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

}

private class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(message: Message) {
            itemView.username.text = message.author
            itemView.msgview.text = message.text
        }
    }

    private class MessageAdapter(
        private val context: Context,
        private val databaseReference: DatabaseReference
    ) : RecyclerView.Adapter<MessageViewHolder>() {

        private val childEventListener: ChildEventListener?

        private val messageIds = ArrayList<String>()
        private val messagelist = ArrayList<Message>()

        init {

            // Create child event listener
            // [START child_event_listener_recycler]
            val childEventListener = object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                    Log.d(TAG, "onChildAdded:" + dataSnapshot.key!!)

                    // A new comment has been added, add it to the displayed list
                    val comment = dataSnapshot.getValue(Message::class.java)

                    // [START_EXCLUDE]
                    // Update RecyclerView
                    messageIds.add(dataSnapshot.key!!)
                    messagelist.add(comment!!)
                    notifyItemInserted(messagelist.size - 1)
                    // [END_EXCLUDE]
                }

                override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                    Log.d(TAG, "onChildChanged: ${dataSnapshot.key}")

                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so displayed the changed comment.
                    val newComment = dataSnapshot.getValue(Message::class.java)
                    val commentKey = dataSnapshot.key

                    // [START_EXCLUDE]
                    val commentIndex = messageIds.indexOf(commentKey)
                    if (commentIndex > -1 && newComment != null) {
                        // Replace with the new data
                        messagelist[commentIndex] = newComment

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
                    val commentIndex = messageIds.indexOf(commentKey)
                    if (commentIndex > -1) {
                        // Remove data from the list
                        messageIds.removeAt(commentIndex)
                        messagelist.removeAt(commentIndex)

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
                    val movedComment = dataSnapshot.getValue(Message::class.java)
                    val commentKey = dataSnapshot.key

                    // ...
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w(TAG, "postComments:onCancelled", databaseError.toException())
                    Toast.makeText(
                        context, "Failed to load comments.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            databaseReference.addChildEventListener(childEventListener)
            // [END child_event_listener_recycler]

            // Store reference to listener so it can be removed on app stop
            this.childEventListener = childEventListener
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
            val inflater = LayoutInflater.from(context)
            val view = inflater.inflate(R.layout.item_message, parent, false)
            return MessageViewHolder(view)
        }

        override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
            holder.bind(messagelist[position])
        }

        override fun getItemCount(): Int = messagelist.size

        fun cleanupListener() {
            childEventListener?.let {
                databaseReference.removeEventListener(it)
            }
        }
    }


