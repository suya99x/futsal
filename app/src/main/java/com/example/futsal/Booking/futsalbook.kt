package com.example.futsal.Booking

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.app.TimePickerDialog
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.example.futsal.R
import com.example.futsal.modal.FutsalBook
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_book.*
import java.text.SimpleDateFormat
import java.util.*


class futsalbook: AppCompatActivity() {
    private var time1: String? = null
    private var time2: String? = null
    private var date: String? = null
    private lateinit var futsalKey: String
    private lateinit var bookref: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book)
        val startTime = findViewById<TextView>(R.id.starttime)
        futsalKey = intent.getStringExtra("fid")
        Log.d("futid", "is" + futsalKey)
        bookref = FirebaseDatabase.getInstance().reference
            .child("booking").child(futsalKey)
        startTime.setOnClickListener {
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                startTime.setText(SimpleDateFormat("HH").format(cal.time))
            }
            TimePickerDialog(
                this,
                timeSetListener,
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            ).show()
        }
        val endtime = findViewById<TextView>(R.id.endtime)
        endtime.setOnClickListener {
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                endtime.setText(SimpleDateFormat("HH").format(cal.time))
            }
            TimePickerDialog(
                this,
                timeSetListener,
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            ).show()
        }
        val mPickTimeBtn = findViewById<Button>(R.id.pickDateBtn)
        val textView = findViewById<TextView>(R.id.dateTv)

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        mPickTimeBtn.setOnClickListener {

            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                // Display Selected date in TextView
                textView.setText("" + dayOfMonth + ", " + month + ", " + year)
            }, year, month, day)
            dpd.show()
        }
        button_book.setOnClickListener {
            time1 = startTime.text.toString()
            time2 = endtime.text.toString()
            date = textView.text.toString()
            if (!TextUtils.isEmpty(time1) && !TextUtils.isEmpty(time2) && !TextUtils.isEmpty(date)) {
                if (time2.toString() < time1.toString()) {
                    Toast.makeText(this@futsalbook, "invalid time ", Toast.LENGTH_SHORT).show()
                } else {
                    bookref.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {  }

                        override fun onDataChange(p0: DataSnapshot) {
                            if (p0.exists()) {
                                bookref.addChildEventListener(object : ChildEventListener {
                                    override fun onCancelled(p0: DatabaseError) {
                                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                    }

                                    override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                    }

                                    override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                                    }

                                    override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                                        val getbook = p0.getValue(FutsalBook::class.java)
                                        val getstartime = getbook!!.initialtime
                                        val getendtime = getbook!!.finaltime
                                        val getdate = getbook!!.bookdate
                                        Log.d("key", "is" + getstartime + getdate)
                                        if (time1== getstartime.toString()  && time2==getendtime.toString()  && date == getdate) {
                                            Toast.makeText(
                                                this@futsalbook,
                                                "already booked at this time ",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            return
                                        } else if ((time1!! <= getstartime.toString() || time1!! >= getendtime.toString()) && (time2!! <= getendtime.toString() || time2!! > time1!!)) {
                                            Toast.makeText(
                                                this@futsalbook,
                                                "booking added..",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            val pushdata = FutsalBook(time1, time2, date)
                                            bookref.push().setValue(pushdata)
                                            return
                                        } else {
                                            Toast.makeText(
                                                this@futsalbook,
                                                "booking added",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            val pushdata = FutsalBook(time1, time2, date)
                                            bookref.push().setValue(pushdata)
                                            return
                                        }
                                    }

                                    override fun onChildRemoved(p0: DataSnapshot) {

                                    }
                                })
                            } else {
                                val pushdata = FutsalBook(time1, time2, date)
                                Toast.makeText(this@futsalbook, "new booking added", Toast.LENGTH_SHORT).show()
                                bookref.push().setValue(pushdata)
                            }
                        }
                    })

                    }

            } else {
                Toast.makeText(this@futsalbook, "enter all details ", Toast.LENGTH_SHORT).show()
            }
        }
        button_close.setOnClickListener {
            Toast.makeText(this@futsalbook, "booking cancelled ", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}












