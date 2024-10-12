package com.example.kotlincomposercameraapp


import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge


import com.example.kotlincomposercameraapp.screens.CameraScreen

import com.example.kotlincomposercameraapp.ui.theme.KotlinComposerCameraAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

       //enableEdgeToEdge()
        setContent {
            KotlinComposerCameraAppTheme{
                CameraScreen()
            }
        }
    }
}


