package com.jondejong.dog

import com.gmongo.GMongo
import com.google.inject.Singleton
import com.jondejong.mongo.DocumentConverter
import ratpack.exec.Blocking
import ratpack.server.Service
import ratpack.server.StartEvent
import ratpack.server.StopEvent


/**
 * Created by jondejong on 9/16/15.
 */
@Singleton
class DogRepository implements Service {

    GMongo mongo
    def database

    @Override
    void onStart(StartEvent event) throws Exception {
        println "Opening MongoDB Connection"
        mongo = new GMongo("127.0.0.1", 27017)
        database = mongo.getDB('doggies')
    }

    @Override
    void onStop(StopEvent event) throws Exception {
        println "Closing MongoDB Connection"
        mongo.close()
    }

    def getDogs() {
        Blocking.get {
            def dogs = []
            def results = database.dogs.find().asList()
            results.each {dog->
                dog.id = dog._id
                dog.remove('_id')
                dogs << dog
            }
            return dogs
        }
    }

    def getDog(id) {
        Blocking.get {
            def dog = database.dogs.findOne([_id: id])
            dog.id = dog._id
            dog.remove('_id')
            dog
        }
    }

    def saveDog(dog) {
        Blocking.get {
            database.dogs << DocumentConverter.convert(dog)
        }
    }

    def delete(id) {
        Blocking.get {
            database.dogs.remove([_id: id])
        }
    }

    def update(dog) {
        Blocking.get {
            database.dogs.update(['_id': dog.id], [$set: DocumentConverter.convert(dog)])
        }
    }
}
