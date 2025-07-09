package com.example.fitnessappc.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.fitnessappc.AuthViewModel
import com.example.fitnessappc.pages.LoginPage
import com.example.fitnessappc.pages.Me
import com.example.fitnessappc.pages.Signup
import com.example.fitnessappc.model.UserProfile

@Composable
fun LoginNavigation(navController: NavHostController, modifier: Modifier = Modifier, authViewModel: AuthViewModel) {
    //val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login", builder = {
        composable("login"){
            LoginPage(modifier,navController,authViewModel)
        }
        composable("signup"){
            Signup(modifier,navController,authViewModel)
        }
        composable("me", arguments = listOf(
            navArgument("user"){
                type = NavType.ParcelableType(UserProfile::class.java)
                nullable = false
            }
        )){ entry ->
            Me(modifier,navController,authViewModel, entry.arguments?.getParcelable("user"))
        }

    })
}