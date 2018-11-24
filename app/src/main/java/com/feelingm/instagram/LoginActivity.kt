package com.feelingm.instagram

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.*
import com.twitter.sdk.android.core.*
import com.twitter.sdk.android.core.identity.TwitterAuthClient
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth

    lateinit var googleSignInClient: GoogleSignInClient

    lateinit var callbackManager: CallbackManager

    val GOOGLE_LOGIN_CODE = 901

    lateinit var twitterAuthClient: TwitterAuthClient

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
        callbackManager = CallbackManager.Factory.create()

        google_sign_in_button.setOnClickListener { googleLogin() }

        facebook_sign_in_button.setOnClickListener { facebookLogin() }

        email_login_button.setOnClickListener { emailLogin() }

        twitter_sign_in_button.setOnClickListener { twitterLogin() }

    }

    fun moveMainPage(user: FirebaseUser?) {
        user?.let {
            Toast.makeText(this, R.string.signin_complete, Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    @SuppressLint("RestrictedApi")
    private fun googleLogin() {
        progress_bar.visibility = View.VISIBLE
        startActivityForResult(googleSignInClient.signInIntent, GOOGLE_LOGIN_CODE)
    }

    private fun facebookLogin() {
        progress_bar.visibility = View.VISIBLE

        LoginManager.getInstance().let {
            it.logInWithReadPermissions(this, mutableListOf("public_profile", "email"))
            it.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {
                    handleFacebookAccessToken(result?.accessToken)
                }

                override fun onCancel() {
                    progress_bar.visibility = View.GONE
                }

                override fun onError(error: FacebookException?) {
                    progress_bar.visibility = View.GONE
                }
            })
        }
    }

    fun createAndLoginEmail() {
        auth.createUserWithEmailAndPassword(email_edittext.text.toString(),
                password_edittext.text.toString())
                .addOnCompleteListener { task ->
                    progress_bar.visibility = View.GONE
                    if (task.isSuccessful) {
                        Toast.makeText(this, R.string.signup_complete, Toast.LENGTH_SHORT).show()
                        moveMainPage(auth.currentUser)
                    } else if (task.exception?.message.isNullOrEmpty()) {
                        Toast.makeText(this, task.exception!!.message, Toast.LENGTH_SHORT).show()
                    } else {
                        signInEmail()
                    }

                }
    }

    private fun signInEmail() {
        auth.signInWithEmailAndPassword(email_edittext.text.toString(),
                password_edittext.text.toString())
                .addOnCompleteListener { task ->
                    progress_bar.visibility = View.GONE

                    if (task.isSuccessful) {
                        moveMainPage(auth.currentUser)
                    } else {
                        Toast.makeText(this, task.exception!!.message, Toast.LENGTH_SHORT).show()
                    }
                }
    }

    private fun emailLogin() {
        if (email_edittext.text.toString().isEmpty() ||
                        password_edittext.text.toString().isEmpty()) {
            Toast.makeText(this, R.string.signout_fail_null, Toast.LENGTH_SHORT).show()
        } else {
            progress_bar.visibility = View.VISIBLE
            createAndLoginEmail()
        }
    }

    private fun twitterLogin() {
        progress_bar.visibility = View.VISIBLE
        twitterAuthClient.authorize(this, object : Callback<TwitterSession>() {
            override fun success(result: Result<TwitterSession>?) {

                val credential = TwitterAuthProvider.getCredential(
                        result?.data?.authToken?.token!!,
                        result.data?.authToken?.secret!!)

                auth.signInWithCredential(credential).addOnCompleteListener { task ->
                    progress_bar.visibility = View.GONE

                    if (task.isSuccessful) {
                        moveMainPage(auth.currentUser)
                    }
                }
            }

            override fun failure(exception: TwitterException?) {
            }
        })
    }

    private fun handleFacebookAccessToken(accessToken: AccessToken?) {
        val credential = FacebookAuthProvider.getCredential(accessToken?.token!!)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            progress_bar.visibility = View.GONE

            if (task.isSuccessful) {
                moveMainPage(auth.currentUser)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        callbackManager.onActivityResult(requestCode, resultCode, data)

        twitterAuthClient.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GOOGLE_LOGIN_CODE) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {
                val account = result.signInAccount
                firebaseAuthWithGoogle(account!!)
            } else {
                progress_bar.visibility = View.GONE
            } }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            progress_bar.visibility = View.GONE

            if (task.isSuccessful) {
                moveMainPage(auth.currentUser)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        moveMainPage(auth.currentUser)
    }
}
