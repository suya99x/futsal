package com.example.futsal.activities


import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.futsal.R
import com.example.futsal.modal.Comment
import com.example.futsal.modal.RatingModel
import com.google.firebase.database.*
import com.google.firebase.database.DataSnapshot
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_recommendation.*
import kotlinx.android.synthetic.main.recommend_content.view.*


class RecommendationActivity: AppCompatActivity() {

    val ref = FirebaseDatabase.getInstance().getReference()
    private lateinit var commentsReference: DatabaseReference
    val dadapter= GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recommendation)
        supportActionBar?.title = "recommendation"
        val commentmgr= LinearLayoutManager(this)
        commentmgr.reverseLayout=true
        commentmgr.stackFromEnd=true
        recycler_recommendation.layoutManager=commentmgr
        recycler_recommendation.adapter=dadapter
        fetchcomment()
    }

    private fun fetchcomment(){
        commentsReference = FirebaseDatabase.getInstance().reference
            .child("comment")
        commentsReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    Log.d("key", "onChild:" + it.key)
                    val commentref = ref.child("comment").child(it.key.toString())
                    commentref.addChildEventListener(object : ChildEventListener {
                        override fun onChildRemoved(p0: DataSnapshot) {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                        override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                            Log.d("comment","on child added"+p0.key)
                            var commentPassing = p0.getValue(Comment::class.java)
                            Log.d("comment", "trying to fetch database:${commentPassing?.uid}")
                            if (commentPassing!=null)
                            {
                                val recommendationvalue:Double=RecommendationCheck(commentPassing.text)
                                if (recommendationvalue>0.5){dadapter.add(additem(commentPassing))}
                            }
                        }

                        override fun onCancelled(p0: DatabaseError) {}

                        override fun onChildMoved(p0: DataSnapshot, p1: String?) {}

                        override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                            Log.d("comment","on child added"+p0.key)
                            var commentPassing = p0.getValue(Comment::class.java)
                            Log.d("comment", "trying to fetch database:${commentPassing?.uid}")
                            if (commentPassing!=null)
                            {
                                val recommendationvalue:Double=RecommendationCheck(commentPassing.text)
                                if (recommendationvalue>0.5){dadapter.add(additem(commentPassing))}
                            }
                        }
                    })
                }
            }
        })
    }
    private fun RecommendationCheck(Discussioncontent:String?): Double {
        val topic="good nice cheap excellent great positive marvelous favorable acceptable wonderful superb splenid worthy"
        val recommendcontent=Discussioncontent
        val splittertopics= topic!!.split(" ")
        val splittedcomment= recommendcontent!!.split(" ")
        val newdisct=splittedcomment.groupingBy { it }.eachCount().filter { it.value > 0 }
        val count: MutableList<Int> = mutableListOf<Int>()
        val count1: MutableList<Int> = mutableListOf<Int>()

        var index1=0
        for (values in newdisct) {

            val word2=values.key
            if (word2 in splittertopics)
            {
                count1.add(1)
                count.add(values.value)
                index1 += 1
            }
            else{
                count1.add(0)
                count.add(values.value)
                index1 += 1
            }

        }

        var u=0.0
        var d1=0.0
        var d2=0.0
        var similarity=0.0

        for (iterate in 0 until index1)
        {
            u += (count1[iterate] * count[iterate])
            d1 += (count1[iterate] * count1[iterate])
            d2 += (count[iterate] * count[iterate])
        }

        val D1=Math.sqrt(d1)
        val D2=Math.sqrt(d2)
        val D=D1*D2
        similarity=u/D

        Log.d("recommendation","the similarity is:$similarity")
        return similarity

    }

}

class additem(val recommend: Comment): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.itemView.recplaceName.text=recommend.name
        viewHolder.itemView.recplacedescription.text=recommend.text


    }
    override fun getLayout(): Int {
        return R.layout.recommend_content
    }

}