package com.abdelfattah.receipts;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    final int GREEN = Color.rgb(20, 105, 61);
    final int RED = Color.rgb(163, 42, 48);
    final int GOLD = Color.rgb(190, 145, 45);
    DB db;
    LinearLayout root, list;
    EditText search;
    TextView totals;
    Uri chosenImage = null;
    static final int PICK_IMAGE = 22;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        db = new DB(this);
        buildUI();
        refresh("");
    }

    TextView text(String s, int size, int color) {
        TextView t = new TextView(this);
        t.setText(s); t.setTextSize(size); t.setTextColor(color);
        t.setGravity(Gravity.CENTER);
        t.setPadding(10, 10, 10, 10);
        return t;
    }
    Button button(String label, int color) {
        Button b = new Button(this);
        b.setText(label); b.setTextColor(Color.WHITE);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));
        return b;
    }
    EditText field(String hint, int type) {
        EditText e = new EditText(this);
        e.setHint(hint); e.setInputType(type);
        e.setSingleLine(true); e.setPadding(18, 8, 18, 8);
        return e;
    }
    void buildUI() {
        ScrollView sc = new ScrollView(this);
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(18, 14, 18, 18); root.setBackgroundColor(Color.rgb(248,248,245));
        sc.addView(root); setContentView(sc);

        TextView title = text("عبد الفتاح", 29, Color.WHITE);
        title.setTypeface(null, 1); title.setBackgroundColor(GREEN);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));
        TextView subtitle = text("أرشيف إيصالات تحويلات بنكك", 17, GREEN);
        root.addView(subtitle);

        totals = text("إجمالي اليوم: 0 جنيه سوداني", 16, RED);
        totals.setTypeface(null, 1); root.addView(totals);

        Button add = button("+ إضافة إيصال", GREEN);
        root.addView(add);
        add.setOnClickListener(v -> addReceiptDialog());

        search = field("ابحث بالاسم أو الهاتف أو رقم العملية", InputType.TYPE_CLASS_TEXT);
        root.addView(search);
        search.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            public void onTextChanged(CharSequence s,int start,int before,int count){ refresh(s.toString()); }
            public void afterTextChanged(android.text.Editable e){}
        });
        list = new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL);
        root.addView(list);
        TextView foot = text("تُحفظ البيانات على هذا الهاتف فقط. احتفظ بنسخة احتياطية آمنة.", 12, Color.DKGRAY);
        root.addView(foot);
    }

    void addReceiptDialog() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL); box.setPadding(20, 5, 20, 5);
        EditText name = field("اسم العميل", InputType.TYPE_CLASS_TEXT);
        EditText phone = field("رقم الهاتف (اختياري)", InputType.TYPE_CLASS_PHONE);
        EditText amount = field("المبلغ بالجنيه السوداني", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        EditText ref = field("رقم العملية / الإشعار (اختياري)", InputType.TYPE_CLASS_TEXT);
        EditText note = field("ملاحظات (اختياري)", InputType.TYPE_CLASS_TEXT);
        Button photo = button("اختيار صورة الإيصال", RED);
        TextView photoState = text("لم يتم اختيار صورة", 13, Color.DKGRAY);
        box.addView(name); box.addView(phone); box.addView(amount); box.addView(ref); box.addView(note);
        box.addView(photo); box.addView(photoState);
        photo.setOnClickListener(v -> {
            IntentHelper.pick(this, PICK_IMAGE);
        });
        chosenImage = null;
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("إضافة إيصال")
            .setView(box)
            .setNegativeButton("إلغاء", (d,w)->{})
            .setPositiveButton("حفظ", null).create();
        dialog.setOnShowListener(x -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String n=name.getText().toString().trim();
            String a=amount.getText().toString().trim();
            if(n.isEmpty() || a.isEmpty()) {
                Toast.makeText(this, "أدخل اسم العميل والمبلغ", Toast.LENGTH_SHORT).show(); return;
            }
            try {
                ContentValues cv = new ContentValues();
                cv.put("name", n); cv.put("phone", phone.getText().toString().trim());
                cv.put("amount", Double.parseDouble(a)); cv.put("ref", ref.getText().toString().trim());
                cv.put("note", note.getText().toString().trim());
                cv.put("image", chosenImage == null ? "" : chosenImage.toString());
                cv.put("date", new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date()));
                db.getWritableDatabase().insert("receipts", null, cv);
                chosenImage=null; dialog.dismiss(); refresh(search.getText().toString());
                Toast.makeText(this, "تم حفظ الإيصال", Toast.LENGTH_SHORT).show();
            } catch(Exception e) { Toast.makeText(this, "تحقق من المبلغ وأعد المحاولة", Toast.LENGTH_SHORT).show(); }
        }));
        dialog.show();
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PICK_IMAGE && resultCode==RESULT_OK && data!=null) {
            chosenImage=data.getData();
            if(chosenImage!=null) {
                try { getContentResolver().takePersistableUriPermission(chosenImage, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION); } catch(Exception ignored){}
                Toast.makeText(this, "تم اختيار صورة الإيصال", Toast.LENGTH_SHORT).show();
            }
        }
    }

    void refresh(String query) {
        if(list==null) return;
        list.removeAllViews();
        SQLiteDatabase sql=db.getReadableDatabase();
        String q="%"+query+"%";
        Cursor c=sql.rawQuery("SELECT id,name,phone,amount,ref,note,date,image FROM receipts WHERE name LIKE ? OR phone LIKE ? OR ref LIKE ? ORDER BY id DESC", new String[]{q,q,q});
        double today=0;
        String day=new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(new Date());
        Cursor sum=sql.rawQuery("SELECT amount,date FROM receipts",null);
        while(sum.moveToNext()) if(sum.getString(1).startsWith(day)) today+=sum.getDouble(0);
        sum.close();
        totals.setText(String.format(Locale.getDefault(),"إجمالي اليوم: %.2f جنيه سوداني",today));
        if(c.getCount()==0) list.addView(text("لا توجد إيصالات مطابقة بعد.",15,Color.DKGRAY));
        while(c.moveToNext()) {
            final int id=c.getInt(0);
            String nm=c.getString(1), ph=c.getString(2), rf=c.getString(4), nt=c.getString(5), dt=c.getString(6), im=c.getString(7);
            double am=c.getDouble(3);
            LinearLayout card=new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(12,12,12,12); card.setBackgroundColor(Color.WHITE);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.setMargins(0,8,0,8);
            card.setElevation(2); list.addView(card,lp);
            TextView details=text(nm+"\nالمبلغ: "+String.format(Locale.getDefault(),"%.2f",am)+" ج.س.\nالتاريخ: "+dt+
                (ph.isEmpty()?"":"\nالهاتف: "+ph)+(rf.isEmpty()?"":"\nرقم العملية: "+rf)+(nt.isEmpty()?"":"\nملاحظات: "+nt),15,Color.DKGRAY);
            details.setGravity(Gravity.RIGHT); card.addView(details);
            if(!im.isEmpty()) {
                TextView img=text("📎 توجد صورة إيصال مرفقة",13,GREEN); card.addView(img);
                img.setOnClickListener(v -> {
                    try { android.content.Intent i=new android.content.Intent(android.content.Intent.ACTION_VIEW);
                        i.setDataAndType(Uri.parse(im),"image/*"); i.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION); startActivity(i);
                    } catch(Exception ex){ Toast.makeText(this,"تعذر فتح الصورة",Toast.LENGTH_SHORT).show(); }
                });
            }
            Button del=button("حذف الإيصال",RED); card.addView(del);
            del.setOnClickListener(v -> new AlertDialog.Builder(this).setTitle("حذف الإيصال؟")
                .setMessage("لن يمكن التراجع عن الحذف.")
                .setNegativeButton("إلغاء",null).setPositiveButton("حذف",(d,w)->{
                    db.getWritableDatabase().delete("receipts","id=?",new String[]{String.valueOf(id)});
                    refresh(search.getText().toString());
                }).show());
        }
        c.close();
    }

    static class DB extends SQLiteOpenHelper {
        DB(Activity a){super(a,"abdelfattah_receipts.db",null,1);}
        public void onCreate(SQLiteDatabase d){
            d.execSQL("CREATE TABLE receipts (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, phone TEXT, amount REAL NOT NULL, ref TEXT, note TEXT, date TEXT, image TEXT)");
        }
        public void onUpgrade(SQLiteDatabase d,int oldV,int newV){}
    }
    static class IntentHelper {
        static void pick(Activity a,int code){
            android.content.Intent i=new android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT);
            i.addCategory(android.content.Intent.CATEGORY_OPENABLE); i.setType("image/*");
            i.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION | android.content.Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            a.startActivityForResult(i,code);
        }
    }
}
