package com.example.futsal.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.futsal.MainActivity
import com.example.futsal.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class UserDetails: AppCompatActivity(), View.OnClickListener {
    override fun onClick(v: View?) {
    }
    private var useremail: TextView? = null
    private var username: TextView? = null
    private var userphone: TextView? = null
    private var btnsignout: Button? = null
    private var EmailVerifiied: TextView? = null


    private var mDatabaseReference: DatabaseReference? = null
    private var mDatabase: FirebaseDatabase? = null
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)
        initialise()
    }
    private fun initialise() {
        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase!!.reference!!.child("Users")
        mAuth = FirebaseAuth.getInstance()
        EmailVerifiied=findViewById<View>(R.id.user_email_verifiied) as TextView
        btnsignout = findViewById<View>(R.id.button_sign_out) as Button
        username= findViewById<View>(R.id.user_name) as TextView
        userphone= findViewById<View>(R.id.user_phone) as TextView
        useremail= findViewById<View>(R.id.user_email) as TextView
        btnsignout!!.setOnClickListener { signoutuser() }
    }
    override fun onStart() {
        super.onStart()
        val mUser = mAuth!!.currentUser
        val mUserReference = mDatabaseReference!!.child(mUser!!.uid)
        useremail!!.text = mUser.email
        EmailVerifiied!!.text = mUser.isEmailVerified.toString()

        mUserReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(datasnapshot: DataSnapshot) {
                username!!.text = datasnapshot.child("Name").value as String
                userphone!!.text = datasnapshot.child("phone").value as String
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }
    private  fun signoutuser()
    {
        mAuth!!.signOut()
        startActivity(Intent(this, MainActivity::class.java))
    }
}