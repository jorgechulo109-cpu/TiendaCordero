package com.tiendacordero.app

import android.app.*
import android.os.Bundle
import android.content.*
import android.database.sqlite.*
import android.graphics.Color
import android.view.*
import android.widget.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StoreDb(ctx: Context): SQLiteOpenHelper(ctx,"cordero.db",null,1) {
 override fun onCreate(db: SQLiteDatabase) {
  db.execSQL("CREATE TABLE products(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,price REAL NOT NULL,cost REAL NOT NULL,stock INTEGER NOT NULL)")
  db.execSQL("CREATE TABLE sales(id INTEGER PRIMARY KEY AUTOINCREMENT,product TEXT NOT NULL,quantity INTEGER NOT NULL,total REAL NOT NULL,profit REAL NOT NULL,date TEXT NOT NULL)")
 }
 override fun onUpgrade(db:SQLiteDatabase,oldVersion:Int,newVersion:Int) {}
}

class MainActivity: Activity() {
 private lateinit var db:StoreDb
 private lateinit var root:LinearLayout
 private val navy=Color.rgb(14,35,72)
 private val yellow=Color.rgb(255,205,35)
 override fun onCreate(savedInstanceState:Bundle?) { super.onCreate(savedInstanceState); db=StoreDb(this); home() }
 private fun money(n:Double)=String.format(Locale.US,"$%.2f",n)
 private fun layout(title:String) {
  val scroll=ScrollView(this); scroll.setBackgroundColor(Color.rgb(245,247,251))
  root=LinearLayout(this); root.orientation=LinearLayout.VERTICAL; root.setPadding(24,24,24,40); scroll.addView(root); setContentView(scroll)
  val header=TextView(this); header.text="TIENDA CORDERO"; header.textSize=25f; header.setTextColor(navy); header.setTypeface(null,1); root.addView(header)
  val subtitle=TextView(this); subtitle.text=title; subtitle.textSize=19f; subtitle.setPadding(0,14,0,20); root.addView(subtitle)
 }
 private fun button(label:String, action:()->Unit) { val b=Button(this); b.text=label; b.setTextColor(navy); b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(yellow)); root.addView(b); b.setOnClickListener{action()} }
 private fun field(hint:String, numeric:Boolean=false):EditText { val e=EditText(this); e.hint=hint; if(numeric)e.inputType=android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL; root.addView(e); return e }
 private fun line(t:String) { val v=TextView(this); v.text=t; v.textSize=16f; v.setTextColor(navy); v.setPadding(4,12,4,12); root.addView(v) }
 private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_SHORT).show()
 private fun home(){ layout("Administración de tu negocio"); button("📦 Productos e inventario"){products()}; button("➕ Agregar producto"){addProduct()}; button("🛒 Registrar venta"){sell()}; button("📋 Historial de ventas"){history()}; button("📊 Resumen de ganancias"){summary()} }
 private fun addProduct(){ layout("Nuevo producto"); val name=field("Nombre del producto"); val price=field("Precio de venta (ej. 1.50)",true); val cost=field("Costo de compra (ej. 1.00)",true); val stock=field("Cantidad en inventario",true)
  button("Guardar producto") { val p=price.text.toString().toDoubleOrNull(); val c=cost.text.toString().toDoubleOrNull(); val s=stock.text.toString().toIntOrNull(); if(name.text.isBlank()||p==null||c==null||s==null||p<0||c<0||s<0){toast("Revisa los datos")}else{val v=ContentValues();v.put("name",name.text.toString().trim());v.put("price",p);v.put("cost",c);v.put("stock",s);db.writableDatabase.insert("products",null,v);products()} }; button("Volver al inicio"){home()}
 }
 private fun products(){layout("Productos e inventario"); val cur=db.readableDatabase.rawQuery("SELECT id,name,price,cost,stock FROM products ORDER BY name",null);cur.use{while(it.moveToNext()){val id=it.getInt(0);line("${it.getString(1)}\nVenta: ${money(it.getDouble(2))}  Costo: ${money(it.getDouble(3))}\nExistencias: ${it.getInt(4)}");button("Editar existencias / eliminar #$id"){editProduct(id)}}};button("Agregar producto"){addProduct()};button("Inicio"){home()}}
 private fun editProduct(id:Int){val cur=db.readableDatabase.rawQuery("SELECT name,stock FROM products WHERE id=?",arrayOf(id.toString()));if(!cur.moveToFirst()){cur.close();products();return};val name=cur.getString(0);val current=cur.getInt(1);cur.close();layout("Inventario: $name");val amount=field("Existencias actuales: $current",true);button("Guardar existencias"){val n=amount.text.toString().toIntOrNull();if(n==null||n<0)toast("Cantidad inválida")else{val v=ContentValues();v.put("stock",n);db.writableDatabase.update("products",v,"id=?",arrayOf(id.toString()));products()}};button("Eliminar producto"){AlertDialog.Builder(this).setMessage("¿Eliminar $name?").setNegativeButton("Cancelar",null).setPositiveButton("Eliminar"){_,_->db.writableDatabase.delete("products","id=?",arrayOf(id.toString()));products()}.show()};button("Volver"){products()}}
 private fun sell(){layout("Registrar venta");val ids=mutableListOf<Int>();val names=mutableListOf<String>();val prices=mutableListOf<Double>();val costs=mutableListOf<Double>();val stocks=mutableListOf<Int>();db.readableDatabase.rawQuery("SELECT id,name,price,cost,stock FROM products WHERE stock>0 ORDER BY name",null).use{while(it.moveToNext()){ids.add(it.getInt(0));names.add(it.getString(1));prices.add(it.getDouble(2));costs.add(it.getDouble(3));stocks.add(it.getInt(4))}};if(ids.isEmpty()){line("Agrega productos con existencias antes de vender.");button("Inicio"){home()};return};val spinner=Spinner(this);spinner.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,names.mapIndexed{i,n->"$n — ${money(prices[i])} (${stocks[i]} disponibles)"});root.addView(spinner);val quantity=field("Cantidad a vender",true);button("Confirmar venta"){val i=spinner.selectedItemPosition;val q=quantity.text.toString().toIntOrNull();if(q==null||q<=0||q>stocks[i]){toast("Cantidad inválida o sin existencias")}else{val database=db.writableDatabase;try{database.beginTransaction();val check=database.rawQuery("SELECT stock FROM products WHERE id=?",arrayOf(ids[i].toString()));val available=check.use{if(it.moveToFirst())it.getInt(0)else 0};if(available<q)throw IllegalStateException("Existencias insuficientes");val v=ContentValues();v.put("stock",available-q);database.update("products",v,"id=?",arrayOf(ids[i].toString()));val sale=ContentValues();sale.put("product",names[i]);sale.put("quantity",q);sale.put("total",q*prices[i]);sale.put("profit",q*(prices[i]-costs[i]));sale.put("date",SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.getDefault()).format(Date()));database.insertOrThrow("sales",null,sale);database.setTransactionSuccessful();toast("Venta registrada: ${money(q*prices[i])}");}catch(e:Exception){toast(e.message?:"Error al registrar venta")}finally{database.endTransaction()};home()}}};button("Inicio"){home()}}
 private fun history(){layout("Historial de ventas");db.readableDatabase.rawQuery("SELECT date,product,quantity,total FROM sales ORDER BY id DESC LIMIT 200",null).use{while(it.moveToNext())line("${it.getString(0)}  ·  ${it.getString(1)}\nCantidad: ${it.getInt(2)}  Total: ${money(it.getDouble(3))}")};button("Inicio"){home()}}
 private fun summary(){layout("Resumen del negocio");db.readableDatabase.rawQuery("SELECT COUNT(*),COALESCE(SUM(total),0),COALESCE(SUM(profit),0) FROM sales",null).use{if(it.moveToFirst()){line("Ventas registradas: ${it.getInt(0)}");line("Ingresos totales: ${money(it.getDouble(1))}");line("Ganancia bruta estimada: ${money(it.getDouble(2))}")}};val today=SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(Date());db.readableDatabase.rawQuery("SELECT COALESCE(SUM(total),0),COALESCE(SUM(profit),0) FROM sales WHERE date LIKE ?",arrayOf("$today%" )).use{if(it.moveToFirst()){line("Ingresos de hoy: ${money(it.getDouble(0))}");line("Ganancia bruta de hoy: ${money(it.getDouble(1))}")}};line("La ganancia bruta no descuenta gastos generales ni impuestos.");button("Inicio"){home()}}
}
