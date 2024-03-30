function sendX() {
  var info = new Date().toJSON()
  for (let i = 0; i < arguments.length; i++) {
    info += " " + arguments[i];
  }
  console.log(info)
  send(info)
}

function printFields(javaObj) {
  var res = {}
  var fields = javaObj.class.getFields()

  for (var i = 0; i < fields.length; i++) {
    var field = fields[i]
    var name = field.getName()
    field.setAccessible(true)
    var v = field.get(javaObj)
    res[name] = v + ""
  }
  console.warn(JSON.stringify(res))
}

function printDeclaredFields(javaObj) {
  var res = {}
  var fields = javaObj.class.getDeclaredFields()

  for (var i = 0; i < fields.length; i++) {
    var field = fields[i]
    var name = field.getName()
    field.setAccessible(true)
    var v = field.get(javaObj)
    res[name] = v + ""
  }
  console.warn(JSON.stringify(res))
}

function getStackText(log) {
  var Exception = Java.use('java.lang.Exception')
  var Log = Java.use('android.util.Log')
  var exp = Exception.$new("print")
  var text = Log.getStackTraceString(exp)
  if (log) {
    console.error(text)
  }
  return text

}

Java.perform(function () {
  var Fragment = Java.use('androidx.fragment.app.Fragment');
  var Activity = Java.use('android.app.Activity')
  var JavaString = Java.use('java.lang.String');
  var Toast = Java.use('android.widget.Toast')
  var System = Java.use('java.lang.System')
  var ActivityManager = Java.use('android.app.ActivityManager')
  var ArrayList = Java.use('java.util.ArrayList')
  var Bundle = Java.use('android.os.Bundle')
  var RecyclerView = Java.use('androidx.recyclerview.widget.RecyclerView')
  var ListView = Java.use('android.widget.ListView')
  var View = Java.use('android.view.View')
  var Dialog = Java.use('android.app.Dialog')

  //    Activity.onCreate.overload('android.os.Bundle').implementation = function(bundle){
  //        send("before onCreate ..." + this);
  //        Activity.onCreate.call(this,bundle)
  //    }
  //    var Log = Java.use('android.util.Log')
  var SQLiteDatabase = Java.use('com.tencent.wcdb.database.SQLiteDatabase')
  //    SQLiteDatabase.openDatabase.overload(
  //    'java.lang.String',
  //    '[B',
  //    'com.tencent.wcdb.database.SQLiteCipherSpec',
  //    'com.tencent.wcdb.database.SQLiteDatabase$CursorFactory',
  //    'int',
  //    'com.tencent.wcdb.DatabaseErrorHandler',
  //    'int')
  //.implementation = function(path, password, cipher,factory, flags, errorHandler, poolSize){
  //        sendX('db path:', path)
  //        seddx('db passwords:', JavaString.$new(password))
  //    }
  //
  // 截取方法的调用
  Interceptor.attach(SQLiteDatabase.openDatabase.overload(
    'java.lang.String',
    '[B',
    'com.tencent.wcdb.database.SQLiteCipherSpec',
    'com.tencent.wcdb.database.SQLiteDatabase$CursorFactory',
    'int',
    'com.tencent.wcdb.DatabaseErrorHandler',
    'int')
    .implementation, {
    onEnter: function (args) {
      console.log("Static method hooked!");
    }
  });


});