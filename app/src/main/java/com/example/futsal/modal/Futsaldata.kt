package com.example.futsal.modal

import com.google.firebase.database.IgnoreExtraProperties

class User {
    var name: String? = null
    var email: String? = null

    constructor() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    constructor(name: String?, email: String?) {
        this.name = name
        this.email = email
    }
}

data class FutsalModel(
        var id:String?=null,
        var Image: String? = null,
        var Name : String? = null,
        var Description : String? = null
        )

@IgnoreExtraProperties
class Comment{
    var uid: String? = ""
    var name: String? = ""
    var text: String? = ""

    constructor() {
        // Default constructor required for calls to DataSnapshot.getValue(Message.class)
    }

    constructor(uid: String?, name: String?,text: String?) {
        this.uid = uid
        this.name = name
        this.text=text
    }

}

class RatingModel{
    var stars: String?=null
    var name: String? = ""
    var uid:String?=null
    constructor() {
        // Default constructor required for calls to DataSnapshot.getValue(Message.class)
    }
    constructor(stars: String?, name:String?,uid: String?) {
        this.stars = stars
        this.name = name
        this.uid = uid
    }

}

class Message {

    constructor() //empty for firebase

    constructor(uid: String?,author: String?,text: String?) {
        this.uid = uid
        this.author = author
        this.text = text
    }

    var author: String? = null
    var text: String? = null
    var uid: String? = ""
}

class FutsalBook{
    var initialtime: String? = ""
    var finaltime: String? = ""
    var bookdate: String? = ""

    constructor() {
        // Default constructor required for calls to DataSnapshot.getValue(Message.class)
    }

    constructor(inittime: String?, finltime: String?,bookdate: String?) {
        this.initialtime = inittime
        this.finaltime = finltime
        this.bookdate=bookdate
    }

}


