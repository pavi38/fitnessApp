package com.example.fitnessappc

import android.util.Log
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fitnessappc.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthViewModel : ViewModel() {

    private val auth : FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init {
        checkAuthStatus()
    }


    fun checkAuthStatus(){
        if(auth.currentUser==null){
            _authState.value = AuthState.Unauthenticated
        }else{
            fetchUserProfile()
            println(auth.currentUser?.email)
        }
    }

    fun login(email : String,password : String){

        if(email.isEmpty() || password.isEmpty()){
            _authState.value = AuthState.Error("Email or password can't be empty")
            return
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener{task->
                if (task.isSuccessful){
                    fetchUserProfile()
                }else{
                    _authState.value = AuthState.Error(task.exception?.message?:"Something went wrong")
                }
            }
    }

    fun signup(email : String,password : String, height : Int,weight : Int){

        if(email.isEmpty() || password.isEmpty()){
            _authState.value = AuthState.Error("Email or password can't be empty")
            return
        }
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener{task->
                if (task.isSuccessful){
                    createUserProfile(height,weight)
                }else{
                    _authState.value = AuthState.Error(task.exception?.message?:"Something went wrong")
                }
            }
    }

    fun createUserProfile(height : Int,weight : Int){
        val userProfile = UserProfile(height, weight, auth.currentUser?.email ?: "")
        val currentUserId = auth.currentUser?.uid
        if(currentUserId==null){
            _authState.value = AuthState.Error("User not authenticated")
            return
        } else {
            firestore.collection("users")
                .document(currentUserId)
                .set(userProfile)
                .addOnSuccessListener {
                    _authState.value = AuthState.Authenticated(userProfile)
                }
                .addOnFailureListener { e ->
                    _authState.value = AuthState.Error(
                        e.message ?: "Failed to save user profile"
                    )
                }
        }

    }

    fun fetchUserProfile(){
        val currentUserId = auth.currentUser?.uid
        println(currentUserId)
        if(currentUserId==null){
            _authState.value = AuthState.Error("User not authenticated")
            return
        }
        println("hi")
        firestore.collection("users")
            .document(currentUserId)
            .get()
            .addOnCompleteListener { task ->
                println("hi3")
            }
            .addOnSuccessListener { snapshot ->
                println(snapshot.exists())
                if (snapshot.exists()) {
                    // Map the document into your UserProfile class
                    val userProfile = snapshot.toObject(UserProfile::class.java)
                    if (userProfile != null) {
                        println("hi2")
                        _authState.value = AuthState.Authenticated(userProfile)
                    } else {
                        println("failed")
                        _authState.value = AuthState.Error("Failed to parse user profile")
                    }
                } else {
                    _authState.value = AuthState.Error("User profile not found")
                }
            }
            .addOnFailureListener { e ->
                println("failed")
                _authState.value = AuthState.Error(e.message ?: "Error fetching profile")
            }
        println("hi5")
    }

    fun signout(){
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }


}


sealed class AuthState{
    data class Authenticated(val userProfile : UserProfile) : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message : String) : AuthState()
}