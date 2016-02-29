var mongoose = require('mongoose');

  /*
   * Delete '_id' prop of the object and its sub-object recursively
   */
var object_del_id = function(object) {
  if (object != null && typeof(object) != 'string' &&
    typeof(object) != 'number' && typeof(object) != 'boolean') {
    //for array length is defined however for objects length is undefined
    if (typeof(object.length) == 'undefined') {
      delete object._id;
      for (var key in object) {
        if (object.hasOwnProperty(key) && object[key]._id) object_del_id(object[key]); //recursive del calls on object elements
      }
    } else {
      for (var i = 0; i < object.length; i++) {
        object_del_id(object[i]);  //recursive del calls on array elements
      }
    }
  }
};

/*
 * New '_id' prop of the object and its sub-object recursively
 * for deep-copying a mongoose document.
 * Then save the object.
 * http://www.connecto.io/blog/deep-copyingcloning-of-mongoose-object/
 */
var object_new_id = function(object) {
  if (object != null && typeof(object) != 'string' &&
    typeof(object) != 'number' && typeof(object) != 'boolean') {
    //for array length is defined however for objects length is undefined
    if (typeof(object.length) == 'undefined' && '_id' in object) {
      object._id = mongoose.Types.ObjectId();
      object.isNew = true;
      for (var key in object) {
        if (object.hasOwnProperty(key)) object_new_id(object[key]); //recursive del calls on object elements
      }
    } else {
      for (var i = 0; i < object.length; i++) {
        object_new_id(object[i]);  //recursive del calls on array elements
      }
    }
  }
};
module.exports = {
  object_del_id: object_del_id,
  object_new_id: object_new_id};
