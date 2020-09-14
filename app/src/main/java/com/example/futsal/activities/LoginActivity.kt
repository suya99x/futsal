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

//global variables
private var email: String? = null
private var password: String? = null

class LoginActivity : AppCompatActivity(), View.OnClickListener {
    override fun onClick(v: View?) {
    }

    //UI elements
    private var ForgotPassword: AppCompatTextView? = null
    private var etEmail: TextInputEditText? = null
    private var etPassword: TextInputEditText? = null
    private var btnLogin: AppCompatButton? = null
    private var CreateAccount:AppCompatTextView?=null


    //firebase refrence
    private val TAG = "LoginActivity"
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initialise()
    }

    private fun initialise() {
        ForgotPassword = findViewById<View>(R.id.forgot_password) as AppCompatTextView
        etEmail = findViewById<View>(R.id.email_edittext_login) as TextInputEditText
        etPassword = findViewById<View>(R.id.password_edittext_login) as TextInputEditText
        btnLogin = findViewById<View>(R.id.login_button_login) as AppCompatButton
        CreateAccount = findViewById<View>(R.id.back_to_register) as AppCompatTextView
        mAuth = FirebaseAuth.getInstance()
        ForgotPassword!!
            .setOnClickListener { startActivity(Intent(this@LoginActivity, ResetPasswordActivity::class.java)) }
        CreateAccount!!
            .setOnClickListener { startActivity(Intent(this@LoginActivity, RegisterActivity::class.java)) }

        btnLogin!!.setOnClickListener { loginUser() }
    }
    private fun loginUser() {
        email = etEmail?.text.toString()
        password = etPassword?.text.toString()
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            Log.d(TAG, "Logging in user.")
            mAuth!!.signInWithEmailAndPassword(email!!, password!!)
                .addOnCompleteListener(this) { task ->

                    if (task.isSuccessful) {
                        // Sign in success, update UI with signed-in user's information
                        Log.d(TAG, "signInWithEmail:success")
                        updateUI()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.e(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(
                            this@LoginActivity, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } else {
            Toast.makeText(this, "Enter all details", Toast.LENGTH_SHORT).show()
        }
    }
    private fun updateUI() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }
}
