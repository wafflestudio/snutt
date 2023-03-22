import { List } from 'immutable';

import winston = require('winston');
var logger = winston.loggers.get('default');

export function deepCopy<T>(src: T): T {
  return JSON.parse(JSON.stringify(src));
}

export function isNumber(n: any): boolean {
  return typeof n === 'number' && !isNaN(n);
}

function deleteObjectIdRecur(obj: any, stack: List<object>) {
  for (let i=0; i< stack.size; i++) {
    if (i > 10) {
      logger.warn("deleteObjectIdRecur: Too deep stack");
      logger.warn(obj);
      return;
    }
    if (obj === stack[i]) {
      logger.warn("deleteObjectIdRecur: recurrence found");
      logger.warn(obj);
      return;
    }
  }
  if (obj !== null && typeof(obj) == 'object') {
    if (obj instanceof Promise) {
      logger.warn("deleteObjectIdRecur: Object is promise");
    } else if (Array.isArray(obj)) {
      for (let i = 0; i < obj.length; i++) {
        if (obj[i] && obj[i]._id) deleteObjectIdRecur(obj[i], stack.push(obj));  //recursive del calls on array elements
      }
    } else {
      delete obj._id;
      Object.keys(obj).forEach(function(key) {
        if (obj[key]) deleteObjectIdRecur(obj[key], stack.push(obj)); //recursive del calls on object elements
      });
    }
  }
}

  /**
   * Delete '_id' prop of the object and its sub-object recursively
   * This is for copying mongo objects or sanitizing json objects by removing all _id properties
   */
export function deleteObjectId(object) {
  return deleteObjectIdRecur(object, List<object>());
};
