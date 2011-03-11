package com.vesaria.palmachine.db


/**
 * Creates and initializes the Palmachine Database.
 */
object DataInit {
	
	private lazy val conn = DataConnection.connect()
	
	def main(args: Array[String]) {
		println("Initializing database %s.\nWARNING: Old data will be deleted.\nHit Enter to continue or Ctrl-C to abort.".format(DataConnection.dsUrl))
		readLine()
		createSchema()
		if (args.length == 0)
			loadDictionary()
		else
			loadDictionary(args(0))
	}
	
	def createSchema() {
		var stmt = conn.createStatement
		print("Creating schema... ")
		stmt.executeUpdate(DatabaseSchema.sql)
		println (" Done.")
	}
	
	def loadDictionary(words: Iterator[String]) {
		print("Loading dictionary...")
		// [[TODO]] Use a transaction for all this
		clearDictionary()
		words.map(_.trim).filter(! _.isEmpty).foreach(insertWord)
			
		conn.prepareStatement("""
			INSERT INTO r SELECT reverse(w) FROM d;
		""").executeUpdate()
		
		reindexDictionary()
		println(" Done.")
	}
	
	def loadDictionary(filePath: String) { loadDictionary(scala.io.Source.fromFile(filePath).getLines) }
	
	def loadDictionary() { loadDictionary(SmallWordList.words.elements) }
	
	private lazy val insertWordStmt = conn.prepareStatement("""INSERT INTO d (w) VALUES (?)""")
	
	private def insertWord(w: String) {
		insertWordStmt.clearParameters
		insertWordStmt.setString(1, w)
		insertWordStmt.executeUpdate()
	}
	
	private def clearDictionary() {
		conn.prepareStatement("""
			DELETE FROM d;
			DELETE FROM r;
			DELETE FROM transition;
			""").executeUpdate()
	}
	
	private def reindexDictionary() {
		conn.prepareStatement("""
			VACUUM FULL d;
			VACUUM FULL r;
			CLUSTER d_pkey on d;
			CLUSTER r_pkey on r;
			REINDEX TABLE d;
			REINDEX TABLE r;
			ANALYZE d;
			ANALYZE r;
			""").executeUpdate()
	}
	
}


object DatabaseSchema {
		val sql = """
			DROP TABLE IF EXISTS d;
			CREATE TABLE d (
				w VARCHAR PRIMARY KEY
			);
			COMMENT ON TABLE d IS 'wordlist, standard order';
			
			DROP TABLE IF EXISTS r;
			CREATE TABLE r (
				w VARCHAR PRIMARY KEY
			);
			COMMENT ON TABLE r IS 'wordlist, reversed order (denormalized from d)';
			
			DROP TABLE IF EXISTS transition;
			CREATE TABLE transition(
				id SERIAL PRIMARY KEY,
				from_polarity BOOLEAN,
				from_chars VARCHAR,
				to_polarity BOOLEAN,
				to_chars VARCHAR,
				to_is_terminable BOOLEAN,
				via_left_side VARCHAR,
				via_right_side VARCHAR
			);
			COMMENT ON TABLE transition IS 'transitions make up the FSM';

			-- Create indexes after doing INSERTs
			
			CREATE OR REPLACE FUNCTION next_from_pos(varchar) RETURNS SETOF varchar AS $$
				SELECT w FROM r
				WHERE
					w IN
					(select substr($1, 0, generate_series(3,length($1))))
				UNION ALL
			SELECT w FROM r WHERE w LIKE $1 || '%'
			$$
			LANGUAGE SQL
			IMMUTABLE
			RETURNS NULL ON NULL INPUT;
			
			
			CREATE OR REPLACE FUNCTION next_from_neg(varchar) RETURNS SETOF varchar AS $$
				SELECT w FROM d
				WHERE
					w IN
					(select substr($1, 0, generate_series(3,length($1))))
				UNION ALL
			SELECT w FROM d WHERE w LIKE $1 || '%'
			$$
			LANGUAGE SQL
			IMMUTABLE
			RETURNS NULL ON NULL INPUT;
			
			
			CREATE OR REPLACE FUNCTION reverse(TEXT) RETURNS TEXT AS $$
				DECLARE
    				original ALIAS FOR $1;
    				reversed TEXT := '';
    				onechar  VARCHAR;
    				mypos    INTEGER;
  				BEGIN
    				SELECT LENGTH(original) INTO mypos;
    				LOOP
      					EXIT WHEN mypos < 1;
      					SELECT substring(original FROM mypos FOR 1) INTO onechar;
      					reversed := reversed || onechar;
      					mypos := mypos -1;
    				END LOOP;
    				RETURN reversed;
  				END
			$$ LANGUAGE plpgsql IMMUTABLE RETURNS NULL ON NULL INPUT;
		"""
}

/** A small word list, usable for testing */
object SmallWordList {
	val words = List(
		"absolutized","accusant","acetylene","acroleins","administrative","advertizements","aethers",
		"afrit","agapae","aggressivities","alga","amiss","ancillas","aneurin",
		"ankhs","annexe","antennal","antsiest","apace","apneas","apologia",
		"approximative","armories","arums","asdic","asthenics","astronomers","ate",
		"athodyd","attitudinised","auscultation","austerely","authorize","autotrophs","azoth",
		"bacitracin","bagnio","bannerettes","baptise","bastardized","bellyacher","beneficiations",
		"bibb","bights","bijou","blissing","blizzard","blowsiest","boast",
		"bocce","bonks","borohydride","bracteoles","brutalize","caches","caecally",
		"calisaya","campo","cannelloni","canonize","cardiae","carotinoids","casseroles",
		"catalyses","catapult","catenaries","catholicizes","catteries","celluloses","centaur",
		"chamiso","chancier","charriest","chars","chattiest","chautauqua","cheder",
		"chestier","chits","chocoholic","cholesterols","chugging","cimetidine","circus",
		"cirrous","cittern","clach","cleave","clubbing","coagulant","coaxing",
		"coccidium","cockier","colluviums","conferencing","connectivities","contravened","coping",
		"corotation","corporatism","correspondences","coude","crannogs","creamily","crenations",
		"cribber","crispy","crowd","croze","cucumber","cundum","cupreous",
		"curculios","curliest","curricula","curricular","cytoplasm","darshan","daws",
		"decennium","delfs","denotement","deputizations","dernier","detriment","diatheses",
		"difficulties","douma","drek","dumfound","dumfounding","duncical","duplexing",
		"durability","ebonizing","ebullitions","economizes","ecumenicists","educible","embolic",
		"emmenagogue","empiric","enactment","encapsulate","encomiastic","engs","epiglottis",
		"epigrams","epiphenomenal","epistolary","epitaxic","epsilon","evocators","exarchs",
		"excitor","exhalants","extant","extracurriculars","factualities","falls","fellies",
		"feracity","ferrels","festal","fiercer","flabbily","flageolets","flagitious",
		"flat","fleam","flog","florists","foliose","forfeits","frizzier",
		"frotteur","garbanzo","gasses","gastroliths","gaucher","gaudier","gawsy",
		"geez","generatrix","geochemists","geologic","glamouring","gleamy","glomerule",
		"glorify","gonium","granularity","granulocytopoiesis","gravitative","grisly","groats",
		"grudging","gusseting","halakah","halalah","hallooing","harmonising","harsh",
		"heath","heptarch","hest","hobnobber","horrify","hunkering","husking",
		"hydroxy","idiot","illustrates","imago","immanences","immensity","immobilizer",
		"imperatively","impoundments","indictees","indurates","ingurgitated","ingurgitations","inheritances",
		"interiorize","interpret","intimal","intrathoracic","intrauterine","intrepidity","iodating",
		"iodophor","irrepressibility","irreversibility","isoclines","isolating","isonomy","itineraries",
		"janes","jauntier","javelina","jeremiads","jodhpurs","jumps","junkies",
		"justification","kakapos","kaoline","keeling","keet","klephts","knell",
		"knitters","lawlessly","legalize","lemans","lenitions","leopards","levigated",
		"licensable","lifers","lipocytes","lodicule","lyes","lyricising","lysogenizations",
		"lysogeny","mafic","malmiest","mandrel","mangles","masonry","mazily",
		"measurement","megabucks","megacycles","memorializes","memos","men","mentality",
		"mercantile","merman","mesotrons","metal","methodized","monies","monists",
		"monopolizer","mrowl","mucinous","mucros","musk","myriopod","nachas",
		"naphthol","nappers","narcist","narrate","neanderthals","necromancies","neglige",
		"nemerteans","nicotiana","nimiety","nine","nits","nonhomosexuals","nonlegumes",
		"nonmoral","nonparticipating","nonproblems","nonprogressive","notify","novae","novelle",
		"numeric","obtusely","occupy","octal","oligomers","oohing","orienteers",
		"otiosity","ottava","oviposit","oviposits","ozonic","paca","palisading",
		"papist","paraphernalia","parasitize","parse","pasture","pataca","peavey",
		"pelletization","pend","penuckles","personification","phenotypic","phosphofructokinase","phosphoglucomutases",
		"pinnas","pitiful","plasmogamies","pogromists","populistic","pored","posed",
		"potsy","praenomen","preceding","preconceiving","preconcerting","precuts","predeceased",
		"prednisolone","prepotent","presage","presides","previable","prochein","profligately",
		"proprioceptions","psylla","pubs","pylon","pyridoxals","qualification","readily",
		"realists","rectangularity","relict","remnant","retrievable","riels","rillettes",
		"roadster","rondelles","roofing","rooky","routinizations","rubbery","rustier",
		"sacculated","saith","samlet","santims","sawn","sayonara","scabrous",
		"scissoring","sculpture","sectarianized","seems","seigneurs","selectivity","sensitively",
		"sentence","sere","serenities","shew","sicks","silence","skimp",
		"slap","slash","smoky","sneakiest","sociologist","some","spanning",
		"sparse","spelts","sphered","spinosities","splendiferous","spluttering","spoony",
		"staid","stemmiest","stertors","suldans","sulfuring","summarizations","suppressibilities",
		"suricates","swoon","syndicalists","syringing","taipans","tallows","taster",
		"teacherly","teal","technicalization","tellurometer","temples","tenure","tenured",
		"teratomas","tergiversated","theogony","theonomies","therapies","tights","timpano",
		"topaz","toppers","torrefies","toys","tractile","tranks","trepans",
		"trihydroxy","trimmers","trisceles","troffer","trounce","true","tutoring",
		"tuyers","tyke","ufologists","uniparental","unitization","uranyls","urial",
		"urticates","uxoricides","vaginas","varicolored","varicosities","variolar","vasotomies",
		"venerating","verbalizes","vermes","vetiver","viomycin","vitrain","voyageur",
		"westernized","wheedle","whelkiest","when","while","whiles","wingier",
		"winsomely","winsomest","wonner","works","worm","worriting","wost",
		"xenophobe","xeroseres","zinkified"
	)
}