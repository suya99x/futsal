package com.example.futsal.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast
import android.util.Log
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import com.example.futsal.MainActivity
import com.example.futsal.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

//global variables
private var Name: String? = null
private var phone: String? = null
private var email: String? = null
private var password: String? = null

class RegisterActivity: AppCompatActivity(), View.OnClickListener {
    override fun onClick(v: View?) {
    }


    //UI elements

    private var etname: TextInputEditText? = null
    private var etphone: TextInputEditText? = null
    private var etEmail: TextInputEditText? = null
    private var etPassword: TextInputEditText? = null
    private var btnCreateAccount: AppCompatButton? = null
    private var loginacct: AppCompatTextView? = null


    //firebase refrence

    private val TAG = "CreateAccount"
    private var mDatabaseReference: DatabaseReference? = null
    private var mDatabase: FirebaseDatabase? = null
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initialise()
    }

    private fun initialise() {
        //initializing ui elements
        etname = findViewById<View>(R.id.username_edittext_register) as TextInputEditText
        etphone = findViewById<View>(R.id.textInputEditTextPhone) as TextInputEditText
        etEmail = findViewById<View>(R.id.email_edittext_register) as TextInputEditText
        etPassword = findViewById<View>(R.id.password_edittext_register) as TextInputEditText
        btnCreateAccount = findViewById<View>(R.id.register_button_register) as AppCompatButton
        loginacct=findViewById<View>(R.id.already_have_account) as AppCompatTextView
        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase!!.reference.child("Users")
        mAuth = FirebaseAuth.getInstance()
        btnCreateAccount!!
            .setOnClickListener { createNewAccount() }
        loginacct!!
            .setOnClickListener { startActivity(Intent(this,LoginActivity::class.java)) }
    }

    private fun createNewAccount() {
        Name = etname?.text.toString()
        phone = etphone?.text.toString()
        email = etEmail?.text.toString()
        password = etPassword?.text.toString()

        if (!TextUtils.isEmpty(Name) && !TextUtils.isEmpty(phone)
            && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)
        ) {
            Log.d(TAG, "Registering User...")
        } else {
            Toast.makeText(this, "Enter all details", Toast.LENGTH_SHORT).show()
        }
        mAuth!!.createUserWithEmailAndPassword(email!!, password!!)
            .addOnCompleteListener(this) { task ->

                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val userId = mAuth!!.currentUser!!.uid

                    verifyEmail()

                    //update user profile information
                    val currentUserDb = mDatabaseReference!!.child(userId)
                    currentUserDb.child("Name").setValue(Name)
                    currentUserDb.child("phone").setValue(phone)
                    updateUserInfoAndUI()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        this@RegisterActivity, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun updateUserInfoAndUI() {
        //start next activity
        val intent = Intent(this@RegisterActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun verifyEmail() {
        val mUser = mAuth!!.currentUser;
        mUser!!.sendEmailVerification()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Verification email sent to " + mUser.getEmail(),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Log.e(TAG, "sendEmailVerification", task.exception)
                    Toast.makeText(
                        this@RegisterActivity,
                        "Failed to send verification email.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}
