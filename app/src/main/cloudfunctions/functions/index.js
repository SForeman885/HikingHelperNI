const functions = require("firebase-functions");

// The Firebase Admin SDK to access Firestore.
const admin = require("firebase-admin");
admin.initializeApp();

// Create and Deploy Your First Cloud Functions
// https://firebase.google.com/docs/functions/write-firebase-functions

exports.calculateUserAverageSpeed = functions.firestore.document("Users/{userId}/Logs/{logId}").onWrite((change, context) => {
    var usersRef = admin.firestore().collection("Users");
    var totalSpeed = 0;
    var count = 0;
    return usersRef.doc(context.params.userId).collection("Logs").get()
        .then(snapshot => {
            snapshot.forEach(doc => {
                var difficultyValue = getDifficultyValue(doc.get("difficulty"));
                var elevationValue = doc.get("elevation");
                var length = doc.get("length");
                // elevation calculation based on Naismiths Rule (see documentation)
                var elevationModifierValue = ((elevationValue * 3.28) / 1000) * 30;
                var timeTakenwithoutElevation = doc.get("timeTaken") - elevationModifierValue;
                var finalTimeTaken = (timeTakenwithoutElevation / 60) * difficultyValue;
                totalSpeed += (length / finalTimeTaken);
                count++;
            });
            usersRef.doc(context.params.userId).get().then(userSnapshot => {
                var hometown = userSnapshot.get("hometown");
                if (count === 0) {
                    console.log("no previous trails found");
                    return usersRef.doc(context.params.userId).set({
                        hometown: hometown
                    });
                }
                else if (count >= 2) { // only have an averagespeed if more than 2 to ensure the value is useful
                    var averageSpeed = totalSpeed / count;
                    return usersRef.doc(context.params.userId).set({
                        averageSpeed: averageSpeed,
                        hometown: hometown
                    });
                }
                else {
                    return usersRef.doc(context.params.userId).set({
                        hometown: hometown
                    });
                }
            });
        })
        .catch(err => {
            console.log('Error getting user logs', err);
        });
});

function getDifficultyValue(difficulty) {
    // if the log is an easy trail we want to 
    // increase the time taken to equal a more standard value
    if (difficulty == "Easy") {
        return 1.25;
    }// for challenging we want to decrease for the same reason
    else if (difficulty == "Challenging") {
        return 0.75;
    }
    else {
        return 1;
    }
}
