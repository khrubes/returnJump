Parse.Cloud.job("emailTask", function(request, response) {
    var Users = Parse.Object.extend("Users");
    var query = new Parse.Query(Users);
    query.find({
        success: function(results) {
            for (var i = 0; i < results.length; i++) {
                console.log(i)
                var all_items = null;
                //getting the information required to call the email sender function
                users_email = results[i].get("email");
                if ((results[i].get("notifyEmail") === true) && !(users_email === undefined || users_email === "")) {
                    console.log("in if statement")
                    console.log(users_email)
                    //acquiring the email adress
                    //acquiring the items that are about to expire
                    user_install_id = results[i].get("installationObjectId");
                    Parse.Cloud.run("execute", {
                        ObjectId: user_install_id,
                        email: users_email
                    }); 
                    if (i== results.length -1){
                        response.success("Done");
                    }

                } else {
                    if (i == results.length - 1) {
                        console.log(i);
                        response.success("Done");
                    }
                }
            }
        },
        error: function(error) {
            response.error("Error" + error.code + " " + error.message);
        }
    });
});

Parse.Cloud.define('execute', function(request, response) {
    Parse.Cloud.run("getExpired", {
        user_param: request.params.ObjectId
    }).then(function(expiredItems) {
        console.log("in the promise");
        if (!(expiredItems === "")) {
            Parse.Cloud.run("sendemails", {
                address: request.params.email,
                Expiring: expiredItems
            }, {
                success: function() {
                    response.success("Emails sent");
                },
                error: function(error) {
                    response.error("Error" + error.code + " " + error.message);
                }
            });
        }
          else{
            response.success("no items to notify of");
            }
    }), function(error){
        response.error("Error" + error.code + " " + error.message);
    }
});

Parse.Cloud.define("getExpired", function(request, response) {
    //date format
    var d = new Date();
    var curr_date = d.getDate();
    var curr_month = d.getMonth() + 1; //Months are zero based
    var curr_year = d.getFullYear();
    if (parseInt(curr_date) < 10) {
        curr_date = "0" + curr_date;
    };
    if (parseInt(curr_month) < 10) {
        curr_month = "0" + curr_month;
    };
    var date_string = curr_year + "-" + curr_month + "-" + curr_date;

    //query for items expiring on the specified date
    var User_Data = Parse.Object.extend("User_" + request.params.user_param);
    var query = new Parse.Query(User_Data);
    query.equalTo("expiryDate", date_string);
    query.find({
        success: function(results) {
            var namesOfExpiringItems = "";
            for (i = 0; i < results.length; i++) {
                // get the names of the items that are expiring
                namesOfExpiringItems += results[i].get("foodItem") + " ";
                // update the item to notified.
                // results[i].save(null, {success: function(){
                //     results[i].set("notifiedEmail", true);
                //     results[i].save();
                // }})
            }
            response.success(namesOfExpiringItems);
        },
        error: function() {
            response.error("Something went wrong");
        }
    });
});



Parse.Cloud.define("sendemails", function(request, response) {
    var Mandrill = require('mandrill');
    Mandrill.initialize('Insert your key here');
    var expiringItems = request.params.Expiring;
    var verb = null;
    if (expiringItems.split(" ").length > 1) {
        verb = "are ";
    } else {
        verb = "is ";
    };

    Mandrill.sendEmail({
        message: {
            text: "Hello Dear User: The following item: " + expiringItems + verb + "about to expire",
            subject: "SpoilFoil Notifications",
            from_email: "parse@cloudcode.com",
            from_name: "Cloud Code",
            to: [{
                email: request.params.address,
                name: "Arturo"
            }]
        },
        async: true
    }, {
        success: function() {
            response.success("Email sent!");
        },
        error: function() {
            response.error("Uh oh, something went wrong");
        }
    });
})