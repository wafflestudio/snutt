var mongoose = require('mongoose');
var Schema = mongoose.Schema;
//FIXME
var NotificationSchema = mongoose.Schema({
    user_id : { type: Schema.Types.ObjectId, ref: 'User' },
    message : { type : String, required : true },
    created_at : { type : Date, required : true},
    checked : { type : Boolean, required : true, default : false}
});

module.exports = mongoose.model('Notification', NotificationSchema);
