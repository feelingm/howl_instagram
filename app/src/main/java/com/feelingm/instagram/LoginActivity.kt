package com.feelingm.instagram

import android.annotation.SuppressLint
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.TwitterAuthProvider
import com.twitter.sdk.android.core.Result
import com.twitter.sdk.android.core.TwitterException
import com.twitter.sdk.android.core.TwitterSession
import com.twitter.sdk.android.core.identity.TwitterAuthClient
import kotlinx.android.synthetic.main.activity_login.*
import java.sql.ClientInfoStatus

class LoginActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth

    lateinit var googleSignInClient: GoogleSignInClient

    lateinit var callbackManager: CallbackManager

    lateinit var twitterAuthClient: TwitterAuthClient

    val GOOGLE_LOGIN_CODE = 9000

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        google_sign_in_button.setOnClickListener { googleLogin() }

        facebook_sign_in_button.setOnClickListener { facebookLogin() }

        email_login_button.setOnClickListener { emailLogin() }

        twitter_sign_in_button.setOnClickListener { twitterLogin() }
    }

    fun moveMainPage(user: FirebaseUser?) {
        user?.let {
            Toast.makeText(this, getString(R.string.signin_complete), Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    @SuppressLint("RestrictedApi")
    private fun googleLogin() {
        progress_bar.visibility = View.VISIBLE

        startActivityForResult(googleSignInClient.signInIntent, GOOGLE_LOGIN_CODE)
    }

    private fun twitterLogin() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun emailLogin() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun facebookLogin() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
