package com.example.monmenu

import android.app.*
import android.os.Bundle
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.view.*
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

data class Recipe(val name:String,val ingredients:List<String>,val category:String,val time:Int,val tags:String,val budget:String="€")

class MainActivity : Activity() {
    private val recipes = listOf(
        Recipe("Chorba frik",listOf("viande","tomate","oignon","pois chiches","frik"),"Algérien",60,"Ramadan,Hiver","€€"),
        Recipe("Couscous poulet",listOf("couscous","poulet","carotte","courgette","pois chiches"),"Algérien",75,"Toutes","€€"),
        Recipe("Rechta poulet",listOf("rechta","poulet","navet","pois chiches","oignon"),"Algérien",70,"Toutes","€€"),
        Recipe("Tajine zitoune",listOf("poulet","olives","oignon","carotte","citron"),"Algérien",50,"Toutes","€€"),
        Recipe("Bourek viande",listOf("feuille de brick","viande","oignon","fromage"),"Algérien",35,"Ramadan","€€"),
        Recipe("Tajine de légumes",listOf("courgette","carotte","pomme de terre","tomate","oignon"),"Maison",40,"Toutes","€"),
        Recipe("Poulet rôti pommes de terre",listOf("poulet","pomme de terre","oignon","épices"),"Maison",60,"Toutes","€€"),
        Recipe("Gratin pommes de terre",listOf("pomme de terre","poulet","fromage","lait"),"Maison",55,"Hiver","€€"),
        Recipe("Salade composée",listOf("tomate","concombre","salade","œuf","thon"),"Rapide",15,"Été","€"),
        Recipe("Omelette légumes",listOf("œuf","oignon","tomate","poivron"),"Rapide",15,"Toutes","€"),
        Recipe("Pâtes sauce tomate",listOf("pâtes","tomate","oignon","fromage"),"Rapide",25,"Toutes","€"),
        Recipe("Riz au poulet",listOf("riz","poulet","carotte","oignon"),"Maison",35,"Toutes","€€"),
        Recipe("Harira",listOf("viande","tomate","lentilles","pois chiches","céleri"),"Maghreb",60,"Ramadan,Hiver","€€"),
        Recipe("Mhadjeb",listOf("semoule","farine","tomate","oignon","poivron"),"Algérien",45,"Toutes","€")
    )
    private val prefs by lazy { getSharedPreferences("fika",0) }
    private lateinit var content: LinearLayout
    private fun tv(t:String,size:Float=17f,bold:Boolean=false)=TextView(this).apply{ text=t;textSize=size;setTextColor(Color.rgb(65,50,44));if(bold)setTypeface(null,1);setPadding(14,12,14,12)}
    private fun btn(t:String,a:()->Unit)=Button(this).apply{text=t;isAllCaps=false;setOnClickListener{a()}}
    private fun base(title:String,subtitle:String?=null){
        val scroll=ScrollView(this)
        content=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(12,18,12,12)}
        content.addView(tv(title,28f,true)); subtitle?.let{content.addView(tv(it,15f))}
        scroll.addView(content);setContentView(scroll)
    }
    private fun nav(){
        val r=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
        listOf("🏠" to ::home,"🥕" to ::ingredients,"📖" to ::history,"📅" to ::menu).forEach{(s,a)->r.addView(btn(s,a),LinearLayout.LayoutParams(0,58,1f))}
        content.addView(r)
    }
    private fun season():String=when(Calendar.getInstance().get(Calendar.MONTH)){5,6,7->"Été";11,0,1->"Hiver";else->"Toutes"}
    private fun suggestions():List<Recipe>{
        val h=prefs.getStringSet("history",emptySet())!!.joinToString(" ").lowercase()
        val mode=prefs.getString("mode","Normal")!!
        return recipes.filter{r->(r.tags.contains(season())||r.tags.contains("Toutes")) &&
            (mode!="Rapide"||r.time<=30) && (mode!="Économique"||r.budget=="€")}
            .sortedBy{if(h.contains(it.name.lowercase()))1 else 0}.shuffled()
    }
    private fun home(){
        base("✨ Fika Cuisine","Ton carnet de cuisine qui s'adapte à tes envies.")
        content.addView(tv("🌤️ Saison : ${season()}   •   ${prefs.getString("mode","Normal")}",16f,true))
        content.addView(tv("💫 Tes idées du jour",21f,true))
        suggestions().take(2).forEach{r->content.addView(tv("🍽️ ${r.name}\n⏱️ ${r.time} min  •  ${r.budget}\nIngrédients : ${r.ingredients.joinToString(", ")}",17f))}
        content.addView(btn("🔄 Proposer autre chose"){home()})
        content.addView(btn("✨ Surprends-moi"){val r=recipes.random();showRecipe(r)})
        content.addView(tv("🎛️ Adapter Fika Cuisine",21f,true))
        content.addView(btn("⏱️ Je veux quelque chose de rapide"){prefs.edit().putString("mode","Rapide").apply();home()})
        content.addView(btn("💰 Je veux quelque chose d'économique"){prefs.edit().putString("mode","Économique").apply();home()})
        content.addView(btn("🌙 Mode Ramadan"){prefs.edit().putString("mode","Ramadan").apply();Toast.makeText(this,"Mode Ramadan activé 🌙",Toast.LENGTH_SHORT).show();home()})
        content.addView(btn("🍽️ Mode normal"){prefs.edit().putString("mode","Normal").apply();home()})
        content.addView(tv("🔔 Ton rappel cuisine",19f,true))
        content.addView(btn("➕ J'ai cuisiné aujourd'hui"){addHistory()})
        nav()
    }
    private fun showRecipe(r:Recipe){
        AlertDialog.Builder(this).setTitle("✨ ${r.name}")
            .setMessage("⏱️ ${r.time} min\n💰 ${r.budget}\n\n🥕 ${r.ingredients.joinToString(", ")}\n\nCatégorie : ${r.category}\nTags : ${r.tags}")
            .setPositiveButton("❤️ Favori"){_,_->val f=prefs.getStringSet("fav",emptySet())!!.toMutableSet();f.add(r.name);prefs.edit().putStringSet("fav",f).apply()}
            .setNegativeButton("Fermer",null).show()
    }
    private fun addHistory(){
        val input=EditText(this);input.hint="Ex. couscous + salade"
        AlertDialog.Builder(this).setTitle("🍽️ Qu'as-tu cuisiné ?").setView(input)
            .setPositiveButton("Enregistrer"){_,_->val set=prefs.getStringSet("history",emptySet())!!.toMutableSet();val d=SimpleDateFormat("dd/MM/yyyy",Locale.getDefault()).format(Date());set.add("$d — ${input.text}");prefs.edit().putStringSet("history",set).apply();Toast.makeText(this,"Enregistré ❤️",Toast.LENGTH_SHORT).show()}
            .setNegativeButton("Annuler",null).show()
    }
    private fun history(){
        base("📖 Mon historique","Tous tes repas enregistrés restent sur ton téléphone.")
        prefs.getStringSet("history",emptySet())!!.toList().sortedDescending().forEach{content.addView(tv("🍲 $it"))}
        if(prefs.getStringSet("history",emptySet())!!.isEmpty())content.addView(tv("Ton historique est encore vide. Commence ce soir !"))
        content.addView(btn("➕ Ajouter"){addHistory()});nav()
    }
    private fun ingredients(){
        base("🥕 Mon frigo","Écris les ingrédients séparés par des virgules.")
        val input=EditText(this);input.hint="poulet, pomme de terre, tomate..."
        content.addView(input)
        content.addView(btn("🔎 Trouver mes plats"){val w=input.text.toString().lowercase().split(",").map{it.trim()}.filter{it.isNotEmpty()};val res=recipes.map{r->r to w.count{q->r.ingredients.any{it.contains(q)||q.contains(it)}}}.filter{it.second>0}.sortedByDescending{it.second};content.addView(tv("✨ J'ai trouvé ${res.size} idée(s)",20f,true));res.take(10).forEach{(r,n)->content.addView(btn("🍽️ ${r.name} — $n/${w.size} ingrédients"){showRecipe(r)})}})
        content.addView(tv("💡 Plus tu précises tes ingrédients, plus les résultats seront utiles.",14f));nav()
    }
    private fun menu(){
        base("📅 Mon menu","Une proposition pour chaque jour.")
        val ds=listOf("Lundi","Mardi","Mercredi","Jeudi","Vendredi","Samedi","Dimanche");val list=suggestions()
        ds.forEachIndexed{i,d->content.addView(tv("$d  →  🍲 ${list[i%list.size].name}"))}
        content.addView(btn("🔄 Nouveau menu"){menu()});nav()
    }
    override fun onCreate(b:Bundle?){super.onCreate(b);home();if(android.os.Build.VERSION.SDK_INT>=33&&checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=PackageManager.PERMISSION_GRANTED)requestPermissions(arrayOf("android.permission.POST_NOTIFICATIONS"),10)}
}