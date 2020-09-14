package com.example.futsal

import android.content.Context
import android.content.Intent
import android.graphics.ColorSpace
import android.media.Image
import android.os.Bundle
import android.util.EventLogTags
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.futsal.activities.*
import com.example.futsal.modal.FutsalModel
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserInfo
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_hactivity.*

import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.row_places.view.*
import java.util.concurrent.Future


class MainActivity : AppCompatActivity(),NavigationView.OnNavigationItemSelectedListener {
    lateinit var mrecylerview : RecyclerView
    lateinit var ref: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
              start()

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)
    }


    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.user_login -> {
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    Log.v("Session", "Signed In")
                    startActivity(Intent(this, UserDetails::class.java))

                } else {
                    val changePage = Intent(this, LoginActivity::class.java)
                    startActivity(changePage)
                    Toast.makeText(this@MainActivity, "login/sign up", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.navigation_recommendation -> {
                val intent = Intent(this, UserBasedRecommendation::class.java)
                startActivity(intent)
                drawer_layout.closeDrawers()
            }
            R.id.navigation_location -> {
                val intent = Intent(this, LocationActivity::class.java)
                startActivity(intent)
                drawer_layout.closeDrawers()
            }
            R.id.navigation_chat -> {
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    val intent = Intent(this, ChatActivity::class.java)
                    startActivity(intent)
                    drawer_layout.closeDrawers()
                }
                else
                {
                    Toast.makeText(this@MainActivity, "login to chat", Toast.LENGTH_SHORT).show()
                    drawer_layout.closeDrawers()
                }
            }

        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    fun start() {
        ref = FirebaseDatabase.getInstance().getReference().child("futsal")
        mrecylerview = findViewById(R.id.recycler_view)
        mrecylerview.layoutManager = LinearLayoutManager(this)

        firebaseData()
    }

    fun firebaseData() {


        val option = FirebaseRecyclerOptions.Builder<FutsalModel>()
            .setQuery(ref, FutsalModel::class.java)
            .setLifecycleOwner(this)
            .build()


        val firebaseRecyclerAdapter = object: FirebaseRecyclerAdapter<FutsalModel, MyViewHolder>(option) {


            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
                val itemView = LayoutInflater.from(this@MainActivity).inflate(R.layout.row_places,parent,false)
                return MyViewHolder(itemView)
            }


            override fun onBindViewHolder(holder: MyViewHolder, position: Int, model: FutsalModel) {
                val placeid = getRef(position).key.toString()


                ref.child(placeid).addValueEventListener(object : ValueEventListener {

                    override fun onCancelled(p0: DatabaseError) {
                        Toast.makeText(this@MainActivity, "Error Occurred " + p0.toException(), Toast.LENGTH_SHORT)
                            .show()
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        holder.txt_name.setText(model.Name)
                        holder.txt_desc.setText(model.Description)
                        holder.txt_id.setText(model.id)
                        Picasso.with(this@MainActivity).load(model.Image).into(holder.img_vet)
                        holder.itemView.setOnClickListener{
                            val intent=Intent(this@MainActivity,DetailActivity::class.java)
                            intent.putExtra("firebase_image",model.Image)
                            intent.putExtra("firebase_title",model.Name)
                            intent.putExtra("firebase_desc",model.Description)
                            intent.putExtra("firebase_id",model.id)
                            startActivity(intent)
                        }
                        }
                    })
            }
        }
        mrecylerview.adapter = firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening()
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var txt_id: TextView = itemView!!.findViewById(R.id.placeid)
        internal var txt_name: TextView = itemView!!.findViewById(R.id.placeName)
        internal var img_vet: ImageView = itemView!!.findViewById(R.id.placeimage)
        internal var txt_desc: TextView = itemView!!.findViewById(R.id.placedescription)
    }
}






