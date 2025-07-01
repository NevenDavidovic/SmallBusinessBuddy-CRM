# SmallBusinessBuddy-CRM
SmallBusinessBuddy: Desktop CRM za mala poduzeća

# 1. UVOD

## 1.1. Opis problema i motivacija

U današnjem dinamičnom poslovnom okruženju, mala poduzeća, obrtnici, udruge i neprofitne organizacije suočavaju se s rastućim izazovima u upravljanju klijentima i administrativnim procesima. Tradicionalni načini rada koji uključuju ručno vođenje evidencija, generiranje uplatnica pomoću osnovnih alata poput Microsoft Excela te nesistematično praćenje klijenata često rezultiraju gubitkom vremena, povećanim brojem grešaka i neprofesionalnim dojmom prema klijentima.

Posebno problematični su procesi koji uključuju:

**Upravljanje kontaktima i klijentima** - Mnoge male organizacije još uvijek koriste Excel tablice, papirnate kartoteke ili čak bilježnice za praćenje svojih klijenata. Ovakav pristup otežava brzo pronalaženje informacija, ažuriranje podataka i dobivanje pregleda nad aktivnostima klijenata.

**Generiranje i slanje uplatnica** - Ručno kreiranje uplatnica za svakog klijenta pojedinačno izuzetno je vremenski zahtjevan proces. Često se događaju greške u upisivanju podataka, a izgled uplatnica nije uvijek profesionalan ili ujednačen.

**Praćenje polaznika i radionica** -  Bez centralizednog sustava, teško je pratiti koje su radionice održane, koji su polaznici sudjelovali, kao i važne informacije o polaznicima poput dobi (maloljetni ili punoljetni). Ovakav pristup može dovesti do propuštenih obaveza prema roditeljima maloljetnih polaznika, neodgovarajućeg planiranja sadržaja radionica te problema u komunikaciji s polaznicima.

**Komunikacija s klijentima** - Slanje personaliziranih poruka, podsjetnika o plaćanjima ili obavijesti o događajima često se radi ad-hoc načinom, što može rezultirati propuštenim prilikama ili nezadovoljnim klijentima.

Analiza postojećih rješenja na tržištu pokazuje da su dostupni CRM sustavi uglavnom dizajnirani za veća poduzeća i često su prekomplicirani, skupi ili zahtijevaju stalnu internetsku vezu. Mala poduzeća i organizacije trebaju jednostavno, pristupačno rješenje koje će im omogućiti profesionalno upravljanje poslovanjem bez nepotrebne složenosti.

## 1.2. Cilj rada

Glavni cilj ovog rada je razvoj desktop CRM aplikacije **SmallBusinessBuddy** koja će odgovoriti na specifične potrebe malih poduzeća, obrtnika, udruga, neprofitnih organizacija i fizičkih osoba koje samostalno prodaju proizvode ili usluge.

**Specifični ciljevi aplikacije:**

1. **Centralizirano upravljanje klijentima** - Kreiranje jedinstvene baze podataka koja omogućava unos, uređivanje i pretraživanje informacija o klijentima s mogućnostю uvoza postojećih podataka iz Excel ili CSV datoteka.

2. **Automatizacija procesa generiranja uplatnica** - Razvoj sustava koji omogućava brzo kreiranje personaliziranih uplatnica s logom organizacije, automatskim popunjavanjem podataka iz baze klijenata i mogućnostю izvoza u PDF format.

3. **Integrirana email funkcionalnost** - Implementacija mogućnosti slanja uplatnica i personaliziranih poruka direktno iz aplikacije, bilo pojedinačno ili masovno.

4. **Praćenje radionica i polaznika** - Praćenje polaznika na radionicama, vrmena izvođenje radionice te instruktora na njima

5. **Kalendar integracija** - Omogućavanje planiranja događaja, rokova plaćanja i automatskih podsjetnika s mogućnosti sinkronizacije s vanjskim kalendarima.

6. **Offline funkcionalnost** - Razvoj aplikacije koja funkcionira potpuno offline, osim za slanje emailova, čime se osigurava dostupnost i u slučaju problema s internetskom vezom.

7. **Jednostavnost korištenja** - Dizajniranje intuitivnog korisničkog sučelja prilagođenog korisnicima različitih razina tehnološke pismenosti.

**Tehnički ciljevi:**

- Razvoj stabilne desktop aplikacije koristeći JavaFX 
- Implementacija lokalne baze podataka za sigurnu pohranu podataka -> SQLite
- Kreiranje modularnog dizajna koji omogućava buduće proširenje funkcionalnosti


# 2. ANALIZA PROBLEMA I POSTOJEĆIH RJEŠENJA

## 2.1. Potrebe malih poduzeća za CRM sustavom

Mala poduzeća, obrtnici, udruge i neprofitne organizacije predstavljaju značajan dio gospodarstva, ali često nemaju resurse za implementaciju složenih poslovnih sustava. Njihove potrebe za CRM rješenjima razlikuju se od velikih korporacija u nekoliko ključnih aspekata:

**Jednostavnost korištenja** - Vlasnici malih poduzeća i volonteri u udrugama često nemaju tehničko obrazovanje ili vrijeme za učenje složenih sustava. Potrebna su im intuitivna rješenja koja mogu odmah početi koristiti bez opsežnog uvođenja ili obuke.

**Pristupačnost** - Ograničeni proračuni znače da mjesečne pretplate od stotina eura nisu održive opcije. Mala poduzeća traže jednokratne ili niske troškove koji se uklapaju u njihove financijske mogućnosti.

**Offline funkcionalnost** - Za razliku od velikih tvrtki s pouzdanom IT infrastrukturom, mala poduzeća često rade u uvjetima nestabilne internetske veze ili u prostorima gdje internet nije uvijek dostupan. Offline mogućnosti rada postaju kritične.

**Personalizacija** - Svaka organizacija ima svoje specifične potrebe. Sportski klub ima drugačije zahtjeve od organizatora tečajeva kuhanja ili neprofitne udruge. Potrebna je mogućnost prilagodbe osnovnih funkcionalnosti bez složenog programiranja.

**Lokalizacija** - Potreba za podrškom nacionalnih standarda poput hrvatskih uplatnica, specifičnih formata datuma i valute, te lokaliziranog korisničkog sučelja.

### 2.1.1. Specifične potrebe različitih tipova organizacija

**Mali poduzetnici i obrtnici** fokusiraju se na brzo fakturiranje, praćenje obveza klijenata i osnovnu komunikaciju. Trebaju mogućnost brzog generiranja uplatnica s profesionalnim izgledom te evidenciju o tome tko je platio, a tko nije.

**Udruge i neprofitne organizacije** upravljaju članarinama, donacijama i volonterskim aktivnostima. Posebno je važno praćenje statusa članstva, povijesti plaćanja članarine te komunikacija s velikim brojem članova odjednom.

**Klubovi (sportski, kulturni)** trebaju upravljati članarinama, registracijama za događaje, praćenjem sudjelovanja na aktivnostima te komunikacijom s roditeljima maloljetnih članova.

**Organizatori događaja i tečajeva** fokusiraju se na registracije polaznika, praćenje kapaciteta, upravljanje listama čekanja te specijalnim zahtjevima za maloljetne polaznike (dozvole roditelja, posebni uvjeti).

**Fizičke osobe koje pružaju usluge** trebaju jednostavno rješenje za praćenje klijenata, terminiranja, fakturiranja i komunikacije bez složenosti poslovnih sustava.

## 2.2. Pregled postojećih rješenja na tržištu

### 2.2.1. Veliki CRM sustavi

**Salesforce**
Salesforce je jedan od najpoznatijih CRM sustava na tržištu, ali je dizajniran za velika i srednja poduzeća.  Dodatno, zahtijeva stalnu internetsku vezu i opsežno uvođenje.

**HubSpot CRM**
HubSpot nudi besplatnu verziju svog CRM-a koja je privlačna malim poduzećima. Međutim, besplatna verzija ima ograničene funkcionalnosti, a naprednije značajke zahtijevaju plaćene planove koji mogu biti skupi. Također je u potpunosti cloud-based što znači da bez interneta nije moguće raditi.

**Microsoft Dynamics 365**
Microsoft-ovo rješenje je snažno ali kompleksno i skupo. Zahtijeva značajnu IT podršku za implementaciju i održavanje.

### 2.2.2. Rješenja za mala poduzeća

**Zoho CRM**
Zoho nudi relativno pristupačne planove, ali i dalje zahtijeva internetsku vezu i ima složeno sučelje s previše opcija za osnovne korisnike. Lokalizacija za hrvatski tržište je ograničena.

**Pipedrive**
Pipedrive se fokusira na jednostavnost, ali je primarno dizajniran za prodajne timove. Nema mogućnosti generiranja hrvatskih uplatnica ili upravljanja članarinama.

**Insightly**
Kombinira CRM s osnovnim mogućnostima upravljanja projektima. Besplatna verzija ograničena je na 2 korisnika. Nema lokalizaciju za hrvatsko tržište.

### 2.2.3. Lokalna i regionalna rješenja

**TimeBill (Hrvatska)**
Hrvatsko rješenje fokusirano na fakturiranje i osnovnu evidenciju. Ima podršku za hrvatske uplatnice, ali je ograničeno u CRM funkcionalnostima.

**Pausal (regionalno)**
Regionale rješenje s dobrom lokalizacijom, ali je fokusirano na veća poduzeća. Nema specifične funkcionalnosti za udruge ili organizatore događaja.

### 2.2.4. Tradicionalni alati

**Microsoft Excel**
Većina malih organizacija još uvijek koristi Excel za vođenje evidencija. Prednosti su jednostavnost i već postojeće znanje korisnika. Nedostaci uključuju nedostatak automatizacije, probleme s verzijama datoteka, nemoguće centralizirano ažuriranje i probleme pri suradnji više korisnika.

**Google Sheets**
Slično Excelu, ali s mogućnostima suradnje u realnom vremenu. Nedostaju mu specijalizirane CRM funkcionalnosti poput automatskog generiranja uplatnica ili naprednog praćenja komunikacije.

**Papirnata evidencija**
Mnoge manje organizacije još uvijek koriste papirnate kartoteke i bilježnice. Ovakav pristup je jednostavan za početak, ali postaje neupravljivu s rastom broja klijenata i aktivnosti.

## 2.3. Usporedba dostupnih rješenja

### 2.3.1. Usporedna tablica značajki

| Značajka | Salesforce | HubSpot | Zoho | Excel | SmallBusinessBuddy |
|----------|------------|---------|------|-------|-------------------|
| Offline rad | Ne | Ne | Ne | Da | Da |
| Hrvatska lokalizacija | Ne | Ne | Djelomično | Da | Da |
| Hrvatski IBAN/uplatnice | Ne | Ne | Ne | Ručno | Da |
| Jednostavnost | Niska | Srednja | Srednja | Visoka | Visoka |
| Kalendar integracija | Da | Da | Da | Ne | Da |
| Email integracija | Da | Da | Da | Ne | Da |
| Upravljanje događajima | Ograničeno | Ograničeno | Da | Ručno | Da |
| Podrška za udruge | Ne | Ne | Ne | Ručno | Da |

### 2.3.2. Analiza prednosti i nedostataka

**Veliki komercijalni CRM sustavi** nude napredne funkcionalnosti i pouzdanost, ali su prekomplicirani i preskupi za mala poduzeća. Nemaju lokalizaciju za hrvatsko tržište i zahtijevaju stalnu internetsku vezu.

**Tradicionalni alati** poput Excela jednostavni su za korištenje, ali nedostaju im automatizacija i napredne mogućnosti upravljanja odnosima s klijentima. Također su skloni greškama i ne omogućavaju lako dijeljenje informacija.

**Regionalna rješenja** imaju bolju lokalizaciju, ali često su još uvijek fokusirana na veća poduzeća i nemaju specifične funkcionalnosti za različite tipove malih organizacija.

## 2.4. Identifikacija nedostataka postojećih rješenja

### 2.4.1. Glavni problemi dostupnih rješenja

**Nedostatak lokalizacije** - Većina međunarodnih rješenja ne podržava hrvatske specifičnosti poput formata uplatnica, IBAN brojeva ili lokaliziranog korisničkog sučelja.

**Ovisnost o internetskoj vezi** - Skoro sva moderna CRM rješenja su cloud-based, što znači da ne funkcioniraju bez internetske veze. Ovo je problem za organizacije koje rade u uvjetima nestabilne veze ili na lokacijama bez interneta.

**Složenost sučelja** - Većina CRM sustava dizajnirana je za prodajne profesionalce ili velike timove, što rezultira kompleksnim sučeljima s previše opcija za osnovne korisnike.

**Troškovi pretplate** - Mjesečni troškovi se naglo povećavaju s brojem korisnika, što čini većinu rješenja nedostupnima za manje organizacije s ograničenim proračunima.

**Nedostatak specifičnih funkcionalnosti** - Postojeća rješenja nemaju ugrađene funkcionalnosti specifične za udruge, klubove ili organizatore događaja, poput upravljanja članarinama ili posebnih zahtjeva za maloljetne polaznike.

### 2.4.2. Prilika za novo rješenje

Analiza postojećih rješenja pokazuje jasnu prazninu na tržištu za CRM sustav koji bi:

- Dizajniran specifično za mala poduzeća i organizacije
- Funkcionira potpuno offline s mogućnostima online komunikacije
- Ima potpunu hrvatsku lokalizaciju
- Nudi jedinstvenu cijenu bez mjesečnih pretplata
- Uključivao specifične funkcionalnosti za različite tipove malih organizacija
- Omogućavao laku migraciju postojećih podataka iz Excel/CSV datoteka

SmallBusinessBuddy predstavlja odgovor na ove identificirane potrebe, kombinirajući jednostavnost tradicionalnih alata s mogućnostima modernih CRM sustava, prilagođeno specifično hrvatskom tržištu i potrebama malih organizacija.

# 3. SPECIFIKACIJA ZAHTJEVA

## 3.1. Funkcionalni zahtjevi

Funkcionalni zahtjevi definiraju što aplikacija SmallBusinessBuddy mora raditi kako bi zadovoljila potrebe korisnika. Zahtjevi su organizirani po glavnim funkcionalnim područjima aplikacije na temelju dizajna baze podataka.

### 3.1.1. Upravljanje organizacijom

**F1 - Osnovni podaci organizacije**
- Aplikacija mora omogućiti unos i uređivanje osnovnih podataka organizacije (naziv, IBAN, adresa)
- Mora postojati mogućnost dodavanja loga organizacije
- Aplikacija mora pohraniti kontaktne informacije (email, telefon)

**F2 - Konfiguracija organizacije**
- Aplikacija mora omogućiti postavljanje zadanih vrijednosti za uplatnice
- Mora postojati mogućnost konfiguracije email potpisa
- Aplikacija mora čuvati povijest promjena organizacijskih podataka

### 3.1.2. Upravljanje kontaktima (punoljetni polaznici/skrbnici)

**F3 - Unos kontakata**
- Aplikacija mora omogućiti ručni unos kontakata s poljima: ime, prezime, datum rođenja, OIB, adresa, email, telefon
- Mora postojati označavanje kontakta kao člana s datumima članstva
- Aplikacija mora automatski izračunavati dob na temelju datuma rođenja

**F4 - Upravljanje članstvom**
- Aplikacija mora omogućiti postavljanje statusa članstva (aktivni/neaktivni član)
- Mora postojati praćenje datuma početka i kraja članstva
- Aplikacija mora automatski označavati istekla članstva

**F5 - Pretraživanje i filtriranje kontakata**
- Aplikacija mora omogućiti pretraživanje kontakata po imenu, prezimenu, emailu ili OIB-u
- Mora postojati mogućnost filtriranja po statusu članstva, dobi ili gradu
- Aplikacija mora podržati napredna pretraživanja s kombinacijom kriterija

### 3.1.3. Upravljanje maloljetnim polaznicima

**F6 - Registracija maloljetnih polaznika**
- Aplikacija mora omogućiti unos maloljetnih polaznika povezanih sa skrbnikom (contact_id)
- Mora postojati unos spola, dobi i dodatnih napomena
- Aplikacija mora automatski označavati maloljetnost na temelju dobi

**F7 - Povezivanje sa skrbnicima**
- Aplikacija mora omogućiti povezivanje svakog maloljetnog polaznika s punoljetnim skrbnikom
- Mora postojati mogućnost da jedan skrbnik ima više maloljetnih polaznika
- Aplikacija mora prikazati sve maloljetne polaznike određenog skrbnika

**F8 - Upravljanje dozvolama**
- Aplikacija mora omogućiti evidenciju posebnih dozvola i ograničenja za maloljetne polaznike
- Mora postojati mogućnost dodavanja napomena o zdravstvenim ograničenjima
- Aplikacija mora upozoravati na posebne uvjete kod registracije maloljetnika na radionice

### 3.1.4. Upravljanje učiteljima/instruktorima

**F9 - Evidencija učitelja**
- Aplikacija mora omogućiti unos i upravljanje podacima učitelja (ime, prezime, email, telefon)
- Mora postojati mogućnost dodavanja specijalizacija i kvalifikacija
- Aplikacija mora čuvati povijest rada učitelja

**F10 - Rasporedba učitelja**
- Aplikacija mora omogućiti dodjeljivanje učitelja radionicama
- Mora postojati pregled svih radionica određenog učitelja
- Aplikacija mora provjeriti dostupnost učitelja za nove radionice

### 3.1.5. Upravljanje radionicama

**F11 - Kreiranje radionica**
- Aplikacija mora omogućiti kreiranje radionica s nazivom, datumom početka i kraja
- Mora postojati mogućnost dodjeljivanja učitelja radionici
- Aplikacija mora omogućiti postavljanje kapaciteta radionice

**F12 - Upravljanje polaznicima radionica**
- Aplikacija mora omogućiti registraciju polaznika (punoljetnih i maloljetnih) na radionice
- Mora postojati evidencija tipa polaznika (ADULT/CHILD)
- Aplikacija mora automatski povezivati maloljetne polaznike s njihovim skrbnicima

**F13 - Praćenje statusa plaćanja**
- Aplikacija mora omogućiti označavanje statusa plaćanja (PENDING, PAID, REFUNDED, CANCELLED)
- Mora postojati mogućnost dodavanja napomena o plaćanju
- Aplikacija mora prikazivati pregled neplaćenih obveza

**F14 - Vremenski rokovi radionica**
- Aplikacija mora prikazivati nadolazeće radionice
- Mora postojati mogućnost filtriranja radionica po datumima
- Aplikacija mora upozoravati na radionice koje počinju uskoro

### 3.1.6. Upravljanje listama i grupama

**F15 - Kreiranje lista**
- Aplikacija mora omogućiti kreiranje prilagođenih lista kontakata
- Mora postojati mogućnost organiziranja lista po mapama/folderima
- Aplikacija mora podržati različite tipove lista (custom, automatske)

**F16 - Upravljanje članstvom u listama**
- Aplikacija mora omogućiti dodavanje i uklanjanje kontakata iz lista
- Mora postojati mogućnost masovnog dodjeljivanja kontakata listama
- Aplikacija mora voditi evidenciju o tome kada je kontakt dodan u listu

**F17 - Soft delete funkcionalnost**
- Aplikacija mora omogućiti "meko" brisanje lista bez trajnog gubitka podataka
- Mora postojati mogućnost vraćanja obrisanih lista
- Aplikacija mora čuvati datum brisanja i omogućiti filtriranje

### 3.1.7. Sustav plaćanja i uplatnica

**F18 - Predlošci plaćanja**
- Aplikacija mora omogućiti kreiranje predložaka plaćanja s iznosom, šifrom, modelom i opisom
- Mora postojati mogućnost povezivanja predložaka s organizacijom
- Aplikacija mora omogućiti uređivanje i dupliciranje postojećih predložaka

**F19 - Generiranje plaćanja**
- Aplikacija mora omogućiti kreiranje plaćanja na temelju predložaka
- Mora postojati povezivanje plaćanja s kontaktom i organizacijom
- Aplikacija mora automatski popunjavati podatke iz predloška

**F20 - Generiranje uplatnica**
- Aplikacija mora generirati hrvatske uplatnice s HUB-3 barkodovima
- Mora postojati personalizacija uplatnica s logom organizacije
- Aplikacija mora omogućiti masovno generiranje uplatnica za grupu polaznika

**F21 - Praćenje statusa plaćanja**
- Aplikacija mora omogućiti ručno označavanje plaćenih uplatnica
- Mora postojati pregled svih neplaćenih obveza
- Aplikacija mora generirati podsjetke za dugovanja

### 3.1.8. Newsletter i komunikacija

**F22 - Newsletter editor**
- Aplikacija mora imati ugrađeni editor za kreiranje newslettera
- Mora postojati mogućnost korištenja predložaka za različite tipove edukacija
- Aplikacija mora omogućiti personalizaciju poruka s imenima primatelja

**F23 - Email distribucija**
- Aplikacija mora omogućiti slanje newslettera na više primatelja odjednom
- Mora postojati mogućnost slanja na cijele liste ili odabrane kontakte
- Aplikacija mora prikazivati status dostave za svaki email

**F24 - Predlošci za edukacije**
- Aplikacija mora uključiti gotove predloške za različite tipove edukacijskih sadržaja
- Mora postojati mogućnost prilagodbe postojećih predložaka
- Aplikacija mora omogućiti kreiranje novih predložaka

### 3.1.9. Statistike i izvještaji

**F25 - Dashboard s ključnim pokazateljima**
- Aplikacija mora prikazati broj ukupnih kontakata, maloljetnih polaznika i radionica
- Mora postojati pregled financijskih pokazatelja (ukupno poslano, plaćeno, dugovanja)
- Aplikacija mora prikazati statistike nadolazećih događaja

**F26 - Rođendanski kalendar**
- Aplikacija mora prikazati nadolazeće rođendane polaznika (punoljetnih i maloljetnih)
- Mora postojati mogućnost filtriranja rođendana po tjednima/mjesecima
- Aplikacija mora omogućiti slanje čestitki za rođendane

**F27 - Statistike radionica**
- Aplikacija mora prikazati najpopularnije radionice i učitelje
- Mora postojati analiza popunjenosti radionica
- Aplikacija mora generirati izvještaje o prihodima po radionicama

**F28 - Analiza polaznika**
- Aplikacija mora prikazati statistike o aktivnosti polaznika
- Mora postojati analiza zadržavanja polaznika kroz vrijeme
- Aplikacija mora identificirati najaktivnije polaznice i skrbnike

## 3.2. Nefunkcionalni zahtjevi

### 3.2.1. Performanse

**NF1 - Brzina odziva**
- Aplikacija mora odgovoriti na korisničke akcije u roku od 2 sekunde
- Pretraživanje kontakata mora biti završeno u roku od 1 sekunde za bazu do 10,000 kontakata
- Generiranje uplatnice mora biti završeno u roku od 3 sekunde

**NF2 - Skalabilnost baze podataka**
- Aplikacija mora podržati bazu od najmanje 5,000 kontakata bez degradacije performansi
- Mora podržati do 1,000 maloljetnih polaznika s povezanim skrbnicima
- Aplikacija mora omogućiti praćenje do 500 aktivnih radionica godišnje

### 3.2.2. Pouzdanost i integritet podataka

**NF3 - Referentni integritet**
- Aplikacija mora održavati referentni integritet između povezanih tablica
- Mora postojati cascade brisanje za povezane zapise (maloljetni polaznici se brišu sa skrbnikom)
- Aplikacija mora automatski ažurirati povezane zapise pri promjenama

**NF4 - Offline funkcionalnost**
- Sve funkcionalnosti osim slanja emailova moraju raditi bez internetske veze
- Aplikacija mora lokalno pohraniti sve podatke u SQLite bazi
- Mora postojati mogućnost rada u potpuno offline okruženju

### 3.2.3. Sigurnost i privatnost

**NF5 - Zaštita osobnih podataka**
- Aplikacija mora poštivati GDPR propise za osobne podatke maloljetnika
- Mora postojati enkripcija osjetljivih podataka poput OIB-a
- Aplikacija mora omogućiti sigurno brisanje osobnih podataka

**NF6 - Kontrola pristupa maloljetnicima**
- Aplikacija mora ograničiti direktan email kontakt s maloljetnim polaznicima
- Mora postojati automatsko preusmjeravanje komunikacije na skrbnike
- Aplikacija mora voditi evidenciju o pristupima podacima maloljetnika


## 3.3. Dizajn baze podataka

### 3.3.1. Glavne entitete

**Organization** - Sadrži osnovne podatke o organizaciji koja koristi aplikaciju
- Naziv, IBAN, adresa, kontaktni podaci
- Logo organizacije (BLOB)
- Timestamp polja za praćenje promjena

**Contacts** - Punoljetni kontakti (skrbnici, članovi, polaznici)
- Osobni podaci, adresa, kontaktni podaci
- Informacije o članstvu (član od/do)
- Povezivanje s maloljetnim polaznicima

**Underaged** - Maloljetni polaznici
- Osnovni podaci s dobi i spolom
- Povezanost sa skrbnikom (contact_id)
- Informacije o članstvu i posebne napomene

**Teachers** - Učitelje i instruktori
- Kontaktni podaci
- Povezivanje s radionicama

**Workshops** - Radionice i događaji
- Naziv, datumi održavanja
- Povezivanje s polaznicima i učiteljima

**Workshop_participants** - Polaznici radionica
- Tip polaznika (ADULT/CHILD)
- Status plaćanja (PENDING, PAID, REFUNDED, CANCELLED)
- Napomene o sudjelovanju

**Payment_templates** - Predlošci plaćanja
- Iznos, šifra, model plaćanja, opis
- Povezanost s organizacijom

**Payments** - Konkretna plaćanja
- Povezanost s kontaktom, organizacijom i predloškom
- Status plaćanja i datum

**Lists** - Prilagođene liste kontakata
- Naziv, opis, tip liste
- Soft delete funkcionalnost
- Organizacija po mapama

### 3.3.2. Ključni odnosi

- **Jedan-na-više**: Jedan skrbnik može imati više maloljetne djece
- **Jedan-na-više**: Jedna radionica može imati više polaznika
- **Mnogostruki**: Polaznici mogu sudjelovati na više radionica
- **Jedan-na-više**: Jedan predložak plaćanja može generirati više plaćanja
- **Constrainti**: Maloljetni polaznici moraju imati povezanog skrbnika

## 3.4. Korisničke priče (User Stories)

### 3.4.1. Upravljanje maloljetnim polaznicima

**US1 - Registracija djeteta s roditeljem**
*Kao organizator dječjih radionica, želim registrirati dijete s podacima roditelja kako bih imao completan uvid u obitelj.*

*Kriteriji prihvaćanja:*
- Mogu unijeti podatke djeteta (ime, dob, spol)
- Mogu povezati dijete s postojećim ili novim roditeljem/skrbnikom
- Aplikacija automatski označava dijete kao maloljetno

**US2 - Komunikacija preko skrbnika**
*Kao organizator, želim da se sva komunikacija s maloljetnim polaznicima preusmjerava na njihove skrbnike.*

*Kriteriji prihvaćanja:*
- Kad šaljem email o radionici, automatski se šalje skrbniku
- Email sadržava informacije o djetetu i radionici
- Skrbnik dobiva sve relevantne informacije

### 3.4.2. Upravljanje radionicama i učiteljima

**US3 - Kreiranje radionice s učiteljem**
*Kao koordinator programa, želim kreirati novu radionicu i dodijeliti joj učitelja.*

*Kriteriji prihvaćanja:*
- Mogu kreirati radionicu s nazivom i datumima
- Mogu odabrati učitelja iz postojeće liste
- Aplikacija provjerava dostupnost učitelja

**US4 - Pregled radionica učitelja**
*Kao administratorica, želim vidjeti sve radionice određenog učitelja kako bih planirala raspored.*

*Kriteriji prihvaćanja:*
- Mogu filtrirati radionice po učitelju
- Vidim sve prošle i nadolazeće radionice
- Mogu exportirati raspored učitelja

### 3.4.3. Sustav plaćanja

**US5 - Kreiranje predloška za čanmarinu**
*Kao tajnik kluba, želim kreirati predložak za godišnju članarinu koji mogu koristiti za sve članove.*

*Kriteriji prihvaćanja:*
- Mogu postaviti iznos, model plaćanja i opis
- Predložak sadržava sve potrebne podatke za uplatnicu
- Mogu koristiti predložak za generiranje plaćanja

**US6 - Masovno generiranje uplatnica**
*Kao blagajnik, želim generirati uplatnice za članarinu za sve članove odjednom.*

*Kriteriji prihvaćanja:*
- Mogu odabrati predložak članarine
- Mogu odabrati listu članova
- Aplikacija generira uplatnice s ispravnim podacima

### 3.4.4. Newsletter i komunikacija

**US7 - Slanje obavijesti o radionici**
*Kao organizator, želim obavijestiti sve zainteresirane o novoj radionici putem newslettera.*

*Kriteriji prihvaćanja:*
- Mogu koristiti predložak za najavu radionice
- Mogu personalizirati poruku s imenima primatelja
- Aplikacija šalje emailove skupno ili pojedinačno

### 3.4.5. Statistike i rođendani

**US8 - Pregled nadolazećih rođendana**
*Kao koordinator programa, želim vidjeti čiji su rođendani ovaj mjesec kako bih mogla čestitati.*

*Kriteriji prihvaćanja:*
- Vidim rođendane i punoljetnih i maloljetnih polaznika
- Mogu filtrirati po vremenskom razdoblju
- Mogu direktno poslati čestitku

**US9 - Statistike radionica**
*Kao direktorica centra, želim vidjeti koje su radionice najpopularnije i najisplativije.*

*Kriteriji prihvaćanja:*
- Vidim broj polaznika po radionicama
- Vidim financijske pokazatelje po radionicama
- Mogu exportirati izvještaje za izvještavanje