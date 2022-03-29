package com.example.rider

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import com.example.rider.ui.StudentLoginActivity
import com.example.rider.ui.VolunteerLoginActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.rider.R
import android.content.DialogInterface
import com.google.android.material.navigation.NavigationView
import androidx.drawerlayout.widget.DrawerLayout
import com.example.rider.ui.VolunteerHomeFragment
import android.view.Menu
import android.view.MenuItem
import com.example.rider.ui.AboutOptionMenuActivity
import com.example.rider.ui.ProfileFragment
import com.example.rider.ui.VolunteerRequestFragment
import com.example.rider.ui.LogoutFragment
import com.example.rider.ui.DonateFragment
import androidx.core.view.GravityCompat
import android.widget.Button
import com.example.rider.ui.StudentSideNavBarActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.rider.ui.StudentAdapter
import java.util.ArrayList
import com.example.rider.model.StudentForm
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.FirebaseFirestore
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import android.util.Log
import android.widget.TextView
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import android.app.ProgressDialog
import com.example.rider.ui.RegisterActivity
import android.text.TextUtils
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import android.widget.Toast
import com.example.rider.ui.VolunteerSideNavBar
import com.example.rider.ui.StudentHomeFragment
import com.example.rider.ui.StudentRequestFragment
import com.google.android.material.card.MaterialCardView
import androidx.viewbinding.ViewBinding
import kotlin.jvm.JvmOverloads
import java.lang.NullPointerException
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewbinding.ViewBindings
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.RadioButton
import androidx.cardview.widget.CardView
import android.widget.FrameLayout
import android.widget.Spinner
import org.junit.Assert
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        Assert.assertEquals(4, (2 + 2).toLong())
    }
}