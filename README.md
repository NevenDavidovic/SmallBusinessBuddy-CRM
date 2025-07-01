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
Salesforce je jedan od najpoznatijih CRM sustava na tržištu, ali je dizajniran za velika i srednja poduzeća. Njegova složenost i cijena (od 25$ mjesečno po korisniku) čine ga neprikladnim za mala poduzeća. Dodatno, zahtijeva stalnu internetsku vezu i opsežno uvođenje.

**HubSpot CRM**
HubSpot nudi besplatnu verziju svog CRM-a koja je privlačna malim poduzećima. Međutim, besplatna verzija ima ograničene funkcionalnosti, a naprednije značajke zahtijevaju plaćene planove koji mogu biti skupi. Također je u potpunosti cloud-based što znači da bez interneta nije moguće raditi.

**Microsoft Dynamics 365**
Microsoft-ovo rješenje je snažno ali kompleksno i skupo. Cijena počinje od 95$ mjesečno po korisniku, što je nedostupno većini malih organizacija. Zahtijeva značajnu IT podršku za implementaciju i održavanje.

### 2.2.2. Rješenja za mala poduzeća

**Zoho CRM**
Zoho nudi relativno pristupačne planove (od 14$ mjesečno), ali i dalje zahtijeva internetsku vezu i ima složeno sučelje s previše opcija za osnovne korisnike. Lokalizacija za hrvatski tržište je ograničena.

**Pipedrive**
Pipedrive se fokusira na jednostavnost, ali je primarno dizajniran za prodajne timove. Nema mogućnosti generiranja hrvatskih uplatnica ili upravljanja članarinama. Cijena od 15$ mjesečno po korisniku može se nakupiti za veće organizacije.

**Insightly**
Kombinira CRM s osnovnim mogućnostima upravljanja projektima. Besplatna verzija ograničena je na 2 korisnika, a plaćeni planovi počinju od 29$ mjesečno. Nema lokalizaciju za hrvatsko tržište.

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

- Bio dizajniran specifično za mala poduzeća i organizacije
- Funkcionirao potpuno offline s mogućnostma online komunikacije
- Imao potpunu hrvatsku lokalizaciju
- Nudilo jedinstvenu cijenu bez mjesečnih pretplata
- Ukljućivao specifične funkcionalnosti za različite tipove malih organizacija
- Omogućavao laku migraciju postojećih podataka iz Excel/CSV datoteka

SmallBusinessBuddy predstavlja odgovor na ove identificirane potrebe, kombinirajući jednostavnost tradicionalnih alata s mogućnostima modernih CRM sustava, prilagođeno specifično hrvatskom tržištu i potrebama malih organizacija.

