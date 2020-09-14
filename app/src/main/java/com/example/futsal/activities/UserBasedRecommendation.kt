package com.example.futsal.activities
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.futsal.R
import com.example.futsal.modal.RatingModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_recommendation.*
import kotlinx.android.synthetic.main.recommend_futsal.view.*
import kotlin.math.abs

class UserBasedRecommendation: AppCompatActivity()
{
    val ref = FirebaseDatabase.getInstance().getReference()
    private lateinit var ratingref: DatabaseReference
    val adapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.userrecommendactivty)
        val ratmgr = LinearLayoutManager(this)
        ratmgr.reverseLayout = true
        ratmgr.stackFromEnd = true
        recycler_recommendation.layoutManager = ratmgr
        recycler_recommendation.adapter = adapter
        fetchrating()
    }

    private fun fetchrating() {
        val userid = FirebaseAuth.getInstance().currentUser!!.uid
        var count = 0
        var total = 0.0
        var useravg=0.0
        var count1=0
        var total1=0.0
        var otheruseravg=0.0
        var currentuserstar=0.0

        ratingref = FirebaseDatabase.getInstance().reference
            .child("ratings")
        ratingref.addValueEventListener(object : ValueEventListener {

            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {

                p0.children.forEach {
                    Log.d("itkey", "fid" + it.key)
                    ratingref.child(it.key.toString()).addChildEventListener(object : ChildEventListener {
                        override fun onCancelled(p0: DatabaseError) {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                        override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                        override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                        override fun onChildRemoved(p0: DataSnapshot) {}

                        override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                            val passrating = p0.getValue(RatingModel::class.java)
                            Log.d("key", "ondata" + p0.key)
                            if (p0.key == userid) {
                                Log.d("userid", "on child added" + p0.key)
                                if (passrating != null) {
                                    count++
                                    total += passrating!!.stars!!.toDouble()
                                    Log.d(
                                        "fetchrating",
                                        "getdata:${passrating?.stars}${passrating?.name}" + "\n" + count + "\n" + total
                                    )
                                    useravg = total / count
                                    Log.d("avg", "is" + useravg)
                                    currentuserstar=passrating.stars!!.toDouble()
                                }

                            }
                            if (p0.key != userid) {
                                Log.d("otheruser", "id" + p0.key)
                                if (passrating != null) {
                                    count1++
                                    total1 += passrating!!.stars!!.toDouble()
                                    Log.d(
                                        "similaruser",
                                        "stars:${passrating?.stars}" + "\n" + count1 + "\n" + total1                                    )
                                    otheruseravg = total1 / count1
                                    Log.d("avg", "is" + otheruseravg)
                                }
                                val recommendationvalue: Double = RecommendationCheck(currentuserstar, passrating!!.stars!!.toDouble(), useravg, otheruseravg)
                                if (recommendationvalue > 0.5) {
                                    adapter.add(addrecommneditem(passrating!!))
                                }
                            }
                        }
                    })
                }
            }
        })
    }


    private fun RecommendationCheck(currentuserrating: Double, otheruserrating: Double, currentuseravg: Double, otheruseravg: Double): Double {
        val a = currentuserrating
        val b = otheruserrating
        Log.d("usersavg", "is" + currentuseravg + "\n" + otheruseravg)
        Log.d("userrating", "is" + a + "\n" + b)
        var num = 0.0
        var userdenominator = 0.0
        var otheruserdenominator = 0.0
        var similarity = 0.0
        var u = abs(a - currentuseravg)
        var v = abs(b - otheruseravg)
        num += u * v
        userdenominator += u * u
        otheruserdenominator += v * v
        if (a >=b) {
            return similarity
        } else {
            similarity = num / (Math.sqrt(userdenominator) * Math.sqrt(otheruserdenominator))
            Log.d("recommendation", "the similarity is:$similarity")
            return similarity
        }
    }
}

class addrecommneditem(val recommended:RatingModel): Item<ViewHolder>()
{
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.futsalname.text = recommended.name
        viewHolder.itemView.futsalstar.text = recommended.stars
        viewHolder.itemView.currentuserid.text=recommended.uid
    }

    override fun getLayout(): Int {
        return R.layout.recommend_futsal
    }
}



