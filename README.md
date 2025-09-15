# 1. UVOD

## 1.2. Cilj rada

Glavni cilj ovog rada je razvoj desktop CRM aplikacije **SmallBusinessBuddy** koja će odgovoriti na specifične potrebe malih poduzeća, obrtnika, udruga, neprofitnih organizacija i fizičkih osoba koje samostalno prodaju proizvode ili usluge.

### Specifični ciljevi aplikacije

1. **Centralizirano upravljanje klijentima**  
   Kreiranje jedinstvene baze podataka koja omogućava unos, uređivanje i pretraživanje informacija o klijentima s mogućnostima uvoza postojećih podataka iz CSV datoteka.

2. **Automatizacija procesa generiranja uplatnica**  
   Razvoj sustava koji omogućava brzo kreiranje personaliziranih uplatnica s logom organizacije, automatskim popunjavanjem podataka iz baze klijenata i mogućnostima izvoza.

3. **Integrirana email funkcionalnost**  
   Implementacija mogućnosti slanja uplatnica i personaliziranih poruka direktno iz aplikacije, bilo pojedinačno ili masovno.

4. **Praćenje radionica i polaznika**  
   Praćenje polaznika na radionicama, vremena izvođenja radionice te instruktora na njima.

5. **Offline funkcionalnost**  
   Razvoj aplikacije koja funkcionira potpuno offline, osim za slanje emailova, čime se osigurava dostupnost i u slučaju problema s internetskom vezom.

6. **Jednostavnost korištenja**  
   Dizajniranje intuitivnog korisničkog sučelja prilagođenog korisnicima različitih razina tehnološke pismenosti.

### Tehnički ciljevi

- Razvoj stabilne desktop aplikacije koristeći **JavaFX**
- Implementacija lokalne baze podataka za sigurnu pohranu podataka → **SQLite**
- Kreiranje modularnog dizajna koji omogućava buduće proširenje funkcionalnosti

---

# 2. ANALIZA PROBLEMA I POSTOJEĆIH RJEŠENJA

## 2.1. Potrebe malih poduzeća za CRM sustavom

Mala poduzeća, obrtnici, udruge i neprofitne organizacije predstavljaju značajan dio gospodarstva, ali često nemaju resurse za implementaciju složenih poslovnih sustava. Njihove potrebe za CRM rješenjima razlikuju se od velikih korporacija u nekoliko ključnih aspekata:

- **Jednostavnost korištenja** – Vlasnici malih poduzeća i volonteri u udrugama često nemaju tehničko obrazovanje ili vrijeme za učenje složenih sustava. Potrebna su im intuitivna rješenja koja mogu odmah početi koristiti bez opsežnog uvođenja ili obuke.
- **Pristupačnost** – Ograničeni proračuni znače da mjesečne pretplate nisu održive opcije. Mala poduzeća traže jednokratne ili niske troškove koji se uklapaju u njihove financijske mogućnosti.
- **Offline funkcionalnost** – Omogućava nastavak rada čak i ukoliko nestane internetske veze. Prednost je u dostupnosti, no podaci nisu spremljeni u oblaku.
- **Personalizacija** – Svaka organizacija ima svoje specifične potrebe. Potrebna je mogućnost prilagodbe osnovnih funkcionalnosti bez složenog programiranja. Uključuje i personalizaciju predložaka plaćanja te marketinških mailova.
- **Lokalizacija** – Potreba za podrškom nacionalnih standarda poput hrvatskih uplatnica, specifičnih formata datuma i valute, te lokaliziranog korisničkog sučelja.

### 2.1.1. Specifične potrebe različitih tipova organizacija

- **Mali poduzetnici i obrtnici** – fokus na brzo fakturiranje, praćenje broja klijenata i osnovnu komunikaciju.
- **Udruge i neprofitne organizacije** – upravljanje članarinama, donacijama i volonterskim aktivnostima.
- **Klubovi (sportski, kulturni)** – upravljanje članarinama, registracijama za događaje i komunikacijom s roditeljima maloljetnih članova.
- **Organizatori događaja i tečajeva** – registracije polaznika, praćenje kapaciteta i specijalni zahtjevi za maloljetne polaznike.
- **Fizičke osobe** – jednostavno praćenje klijenata, terminiranja, fakturiranja i komunikacije bez složenosti poslovnih sustava.

---

## 2.2. Pregled postojećih rješenja na tržištu

### 2.2.1. Veliki CRM sustavi

- **Salesforce** – jedan od najpoznatijih CRM sustava, namijenjen velikim i srednjim poduzećima. Zahtijeva stalnu internetsku vezu i složeno uvođenje.
- **HubSpot CRM** – besplatna verzija privlačna malim poduzećima, ali s ograničenim funkcionalnostima. Naprednije značajke zahtijevaju plaćene planove. Potpuno cloud-based.

### 2.2.2. Rješenja za mala poduzeća

- **Zoho CRM** – pristupačni planovi, ali složeno sučelje i potrebna internetska veza. Lokalizacija za HR tržište ograničena.
- **Pipedrive** – fokusiran na jednostavnost, primarno za prodajne timove. Nedostaju funkcije za uplatnice i članarine.
- **Insightly** – kombinira CRM i osnovno upravljanje projektima. Besplatna verzija ograničena na 2 korisnika, bez HR lokalizacije.

### 2.2.3. Tradicionalni alati

- **Microsoft Excel** – jednostavan i poznat korisnicima, ali bez automatizacije i integracije s email sustavima.
- **Google Sheets** – podržava suradnju u realnom vremenu, ali nema CRM funkcionalnosti poput automatskog generiranja uplatnica.
- **Papirnata evidencija** – jednostavna za početak, ali neodrživa s rastom organizacije.

---

## 2.3. Identifikacija nedostataka postojećih rješenja

### 2.3.1. Glavni problemi dostupnih rješenja

- Nedostatak lokalizacije (HR formati, IBAN, uplatnice)
- Ovisnost o internetskoj vezi (cloud-based rješenja)
- Složenost sučelja (namijenjena profesionalnim timovima)
- Troškovi pretplate (skupe nadogradnje i korisnici)
- Nedostatak specifičnih funkcionalnosti (udruge, klubovi, maloljetni polaznici)

### 2.3.2. Prilika za novo rješenje

Analiza postojećih rješenja pokazuje jasnu prazninu na tržištu za CRM sustav koji bi:

- Bio dizajniran specifično za mala poduzeća i organizacije
- Funkcionirao potpuno offline s mogućnostima online komunikacije
- Imao potpunu hrvatsku lokalizaciju
- Nudio jedinstvenu cijenu bez mjesečnih pretplata
- Uključivao funkcionalnosti prilagođene različitim tipovima organizacija
- Omogućavao laku migraciju podataka iz Excel/CSV datoteka

**SmallBusinessBuddy** predstavlja odgovor na ove potrebe, kombinirajući jednostavnost tradicionalnih alata s mogućnostima modernih CRM sustava, prilagođen specifično hrvatskom tržištu i potrebama malih organizacija.

# 3. SPECIFIKACIJA ZAHTJEVA

## 3.1. Funkcionalni zahtjevi
Funkcionalni zahtjevi definiraju što aplikacija **SmallBusinessBuddy** mora raditi kako bi zadovoljila potrebe korisnika. Zahtjevi su organizirani po glavnim funkcionalnim područjima aplikacije na temelju dizajna baze podataka.

### 3.1.1. Upravljanje organizacijom
**F1 - Osnovni podaci organizacije**
- Aplikacija mora omogućiti unos i uređivanje osnovnih podataka organizacije (naziv, IBAN, adresa)
- Mora postojati mogućnost dodavanja loga organizacije
- Aplikacija mora pohraniti kontaktne informacije (elektroničke pošte, telefonski broj)

**F2 - Konfiguracija organizacije**
- Aplikacija mora omogućiti postavljanje zadanih vrijednosti za uplatnice
- Mora postojati mogućnost konfiguracije email postavki za slanje
- Aplikacija mora čuvati povijest promjena organizacijskih podataka

### 3.1.2. Upravljanje kontaktima (punoljetni polaznici/skrbnici)
**F3 - Unos kontakata**
- Aplikacija mora omogućiti ručni unos kontakata s poljima: ime, prezime, datum rođenja, OIB, adresa, email, telefon
- Mora postojati označavanje kontakta kao člana s datumima članstva
- Aplikacija mora automatski izračunavati dob na temelju datuma rođenja

**F4 - Upravljanje članstvom**
- Aplikacija mora omogućiti postavljanje statusa članstva (aktivni/neaktivni član)
- Mora postojati praćenje datuma početka i kraja članstva

**F5 - Pretraživanje i filtriranje kontakata**
- Aplikacija mora omogućiti pretraživanje kontakata po imenu, prezimenu, emailu ili OIB-u
- Aplikacija mora podržati napredna pretraživanja s kombinacijom kriterija
- Mogućnost filtriranja po statusu članstva i dobi

### 3.1.3. Upravljanje maloljetnim polaznicima
**F6 - Registracija maloljetnih polaznika**
- Aplikacija mora omogućiti unos maloljetnih polaznika povezanih sa skrbnikom (contact_id)
- Mora postojati unos spola, dobi i dodatnih napomena
- Aplikacija mora automatski označavati maloljetnost na temelju dobi

**F7 - Povezivanje sa skrbnicima**
- Aplikacija mora omogućiti povezivanje svakog maloljetnog polaznika s punoljetnim skrbnikom
- Mora postojati mogućnost da jedan skrbnik ima više maloljetnih polaznika
- Aplikacija mora prikazati sve maloljetne polaznike određenog skrbnika

**F8 - Upravljanje članstvom**
- Aplikacija mora omogućiti evidenciju članstva
- Mora postojati mogućnost dodavanja napomena korisnicima

### 3.1.4. Upravljanje učiteljima/instruktorima
**F9 - Evidencija učitelja**
- Aplikacija mora omogućiti unos i upravljanje podacima učitelja (ime, prezime, email, telefon)
- Aplikacija mora čuvati povijest rada učitelja

**F10 - Raspored učitelja**
- Aplikacija mora omogućiti dodjeljivanje učitelja radionicama

### 3.1.5. Upravljanje radionicama
**F11 - Kreiranje radionica**
- Aplikacija mora omogućiti kreiranje radionica s nazivom, datumom početka i kraja
- Mora postojati mogućnost dodjeljivanja učitelja radionici
- Aplikacija mora pratiti status radionice

**F12 - Upravljanje polaznicima radionica**
- Aplikacija mora omogućiti registraciju polaznika (punoljetnih i maloljetnih) na radionice
- Mora postojati evidencija tipa polaznika (ADULT/CHILD)
- Aplikacija mora automatski povezivati maloljetne polaznike s njihovim skrbnicima

**F13 - Praćenje statusa plaćanja**
- Mora postojati mogućnost dodavanja napomena o plaćanju
- Aplikacija mora prikazivati pregled neplaćenih obveza
- Aplikacija mora omogućiti označavanje statusa plaćanja (PENDING, PAID, REFUNDED, CANCELLED)

**F14 - Vremenski rokovi radionica**
- Mora postojati mogućnost filtriranja radionica po datumima
- Aplikacija mora prikazivati nadolazeće radionice

### 3.1.6. Upravljanje listama i grupama
**F15 - Kreiranje lista**
- Aplikacija mora omogućiti kreiranje prilagođenih lista kontakata

**F16 - Upravljanje članstvom u listama**
- Aplikacija mora omogućiti dodavanje i uklanjanje kontakata iz lista
- Mora postojati mogućnost masovnog dodjeljivanja kontakata listama
- Aplikacija mora voditi evidenciju o tome kada je kontakt dodan u listu

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
- Aplikacija mora generirati hrvatske uplatnice
- Mora postojati personalizacija uplatnica s logom organizacije
- Aplikacija mora omogućiti masovno generiranje uplatnica za grupu polaznika

### 3.1.8. Newsletter i komunikacija
**F21 - Newsletter editor**
- Aplikacija mora imati ugrađeni editor za kreiranje newslettera
- Mora postojati mogućnost korištenja predložaka za različite tipove edukacija
- Aplikacija mora omogućiti personalizaciju poruka s imenima primatelja

**F22 - Email distribucija**
- Aplikacija mora omogućiti slanje newslettera na više primatelja odjednom
- Mora postojati mogućnost slanja na cijele liste ili odabrane kontakte
- Aplikacija mora prikazivati status dostave za svaki email

**F23 - Predlošci za edukacije**
- Aplikacija mora uključiti gotove predloške za različite tipove edukacijskih sadržaja
- Mora postojati mogućnost prilagodbe postojećih predložaka
- Aplikacija mora omogućiti kreiranje novih predložaka

### 3.1.9. Statistike i izvještaji
**F24 - Dashboard s ključnim pokazateljima**
- Aplikacija mora prikazati broj ukupnih kontakata, maloljetnih polaznika i radionica
- Mora postojati pregled financijskih pokazatelja (ukupno poslano, plaćeno, dugovanja)
- Aplikacija mora prikazati statistike nadolazećih događaja

**F25 – Obavijesti**
- Aplikacija mora prikazivati buduće rođendane i radionice

**F26 - Statistike radionica**
- Aplikacija mora prikazati najpopularnije radionice
- Mora postojati analiza popunjenosti radionica
- Aplikacija mora generirati izvještaje o radionicama

**F27 - Analiza polaznika**
- Aplikacija mora prikazati statistike o aktivnosti polaznika
- Mora postojati analiza zadržavanja polaznika kroz vrijeme
- Aplikacija mora identificirati najaktivnije polaznike i skrbnike

---

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

---

## 3.3. Dizajn baze podataka
Aplikacija koristi **SQLite** bazu podataka koja omogućuje lokalno čuvanje svih podataka bez potrebe za vanjskim serverom. Baza podataka je dizajnirana prema principima normalizacije i osigurava integritet podataka kroz odgovarajuće **foreign key constraint-e**.


### 3.3.1. Glavni entiteti
#### Organization (Organizacija)
Osnovna tabela koja čuva podatke o organizaciji koja koristi aplikaciju (naziv, adresa, kontakt podaci, logo).

**Polja:**
- `id` (INTEGER, PRIMARY KEY) – Jedinstveni identifikator
- `name` (TEXT, NOT NULL) – Naziv organizacije
- `IBAN` (TEXT, NOT NULL) – IBAN broj za plaćanja
- `street_name` (TEXT) – Naziv ulice
- `street_num` (TEXT) – Kućni broj
- `postal_code` (TEXT) – Poštanski broj
- `city` (TEXT) – Grad
- `email` (TEXT) – Email adresa organizacije
- `image` (BLOB) – Logo organizacije
- `phone_num` (TEXT) – Telefonski broj
- `created_at` (TEXT) – Datum kreiranja
- `updated_at` (TEXT) – Datum zadnje promjene

#### Contacts (Kontakti/Skrbnici)
Tablica punoljetnih osoba koje mogu biti članovi, skrbnici maloljetnih polaznika ili polaznici radionica.

**Polja:**
- `id` (INTEGER, PRIMARY KEY) – Jedinstveni identifikator
- `first_name` (TEXT, NOT NULL) – Ime
- `last_name` (TEXT, NOT NULL) – Prezime
- `birthday` (TEXT) – Datum rođenja
- `pin` (TEXT) – OIB
- `street_name` (TEXT) – Naziv ulice
- `street_num` (TEXT) – Kućni broj
- `postal_code` (TEXT) – Poštanski broj
- `city` (TEXT) – Grad
- `email` (TEXT) – Email adresa
- `phone_num` (TEXT) – Telefonski broj
- `is_member` (INTEGER, DEFAULT 0) – Je li član (0/1)
- `member_since` (TEXT) – Član od datuma
- `member_until` (TEXT) – Član do datuma
- `created_at` (TEXT) – Datum kreiranja
- `updated_at` (TEXT) – Datum zadnje promjene

#### Underaged (Maloljetni polaznici)
Tablica maloljetnih polaznika koji moraju imati povezanog skrbnika iz tablice **Contacts**.

**Polja:**
- `id` (INTEGER, PRIMARY KEY) – Jedinstveni identifikator
- `first_name` (TEXT, NOT NULL) – Ime
- `last_name` (TEXT, NOT NULL) – Prezime
- `birth_date` (TEXT) – Datum rođenja
- `age` (INTEGER) – Dob
- `pin` (TEXT) – OIB
- `gender` (TEXT) – Spol
- `is_member` (INTEGER, DEFAULT 0) – Je li član (0/1)
- `member_since` (TEXT) – Član od datuma
- `member_until` (TEXT) – Član do datuma
- `note` (TEXT) – Dodatne napomene
- `contact_id` (INTEGER, FK) – Poveznica na skrbnika
- `created_at` (TEXT) – Datum kreiranja
- `updated_at` (TEXT) – Datum zadnje promjene

**Ograničenja:**
- `FOREIGN KEY (contact_id) REFERENCES contacts(id) ON DELETE CASCADE`

#### Teachers (Učitelji/Instruktori)
Tablica koja čuva osnovne podatke o instruktorima, učiteljima i predavačima.

**Polja:**
- `id` (INTEGER, PRIMARY KEY) – Jedinstveni identifikator
- `first_name` (TEXT, NOT NULL) – Ime
- `last_name` (TEXT, NOT NULL) – Prezime
- `email` (TEXT) – Email adresa
- `phone_num` (TEXT) – Telefonski broj
- `created_at` (TEXT) – Datum kreiranja
- `updated_at` (TEXT) – Datum zadnje promjene

#### Workshops (Radionice)
Centralna tablica za upravljanje radionicama, tečajevima i edukacijskim programima.

**Polja:**
- `id` (INTEGER, PRIMARY KEY) – Jedinstveni identifikator
- `name` (TEXT, NOT NULL) – Naziv radionice
- `from_date` (TEXT) – Datum početka
- `to_date` (TEXT) – Datum završetka
- `teacher_id` (INTEGER, FK) – Dodijeljeni učitelj
- `created_at` (TEXT) – Datum kreiranja
- `updated_at` (TEXT) – Datum zadnje promjene

**Ograničenja:**
- `FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE SET NULL`

#### Workshop_participants (Polaznici radionica)
Tablica koja povezuje radionice s njihovim polaznicima (punoljetnim i maloljetnim).

**Polja:**
- `id` (INTEGER, PRIMARY KEY)
- `workshop_id` (INTEGER, NOT NULL, FK)
- `underaged_id` (INTEGER, FK)
- `contact_id` (INTEGER, FK)
- `participant_type` (TEXT, NOT NULL) – 'ADULT' / 'CHILD'
- `payment_status` (TEXT, NOT NULL) – 'PENDING' / 'PAID' / 'REFUNDED' / 'CANCELLED'
- `notes` (TEXT)
- `created_at` (TEXT)
- `updated_at` (TEXT)

**Ograničenja:**
- `CHECK participant_type IN ('ADULT', 'CHILD')`
- `CHECK payment_status IN ('PENDING', 'PAID', 'REFUNDED', 'CANCELLED')`
- `FOREIGN KEY (workshop_id) REFERENCES workshops(id) ON DELETE CASCADE`
- `FOREIGN KEY (underaged_id) REFERENCES underaged(id) ON DELETE CASCADE`
- `FOREIGN KEY (contact_id) REFERENCES contacts(id) ON DELETE CASCADE`

#### Lists (Prilagođene liste)
Omogućuje grupiranje kontakata prema različitim kriterijima.

**Polja:**
- `id` (INTEGER, PRIMARY KEY)
- `name` (TEXT, NOT NULL)
- `description` (TEXT)
- `type` (TEXT, DEFAULT 'CUSTOM')
- `object_type` (TEXT, DEFAULT 'CONTACT')
- `creator` (TEXT)
- `folder` (TEXT)
- `is_deleted` (INTEGER, DEFAULT 0)
- `deleted_at` (TEXT)
- `created_at` (TEXT)
- `updated_at` (TEXT)

#### List_contacts (Članovi lista)
Veza između kontakata i lista.

**Polja:**
- `id` (INTEGER, PRIMARY KEY)
- `list_id` (INTEGER, NOT NULL, FK)
- `contact_id` (INTEGER, NOT NULL, FK)
- `added_at` (TEXT)

**Ograničenja:**
- `UNIQUE(list_id, contact_id)`
- `FOREIGN KEY (list_id) REFERENCES lists(id) ON DELETE CASCADE`
- `FOREIGN KEY (contact_id) REFERENCES contacts(id) ON DELETE CASCADE`

---

### 3.3.2. Sustav plaćanja

#### Payment_template (Predlošci plaćanja)
Tablica standardiziranih predložaka za plaćanja koja omogućuje kreiranje unaprijed definiranih parametara plaćanja (iznos, model, poziv na broj) koji se mogu koristiti pri generiranju uplatnica za različite usluge i članarine, neovisno o konkretnim kontaktima kojima se šalje.

**Polja:**
- `id` (INTEGER, PRIMARY KEY) – Jedinstveni identifikator
- `name` (TEXT, NOT NULL) – Naziv predloška
- `description` (TEXT) – Opis predloška
- `amount` (DECIMAL(10,2), NOT NULL) – Iznos
- `model_of_payment` (TEXT, NOT NULL) – Model plaćanja
- `poziv_na_broj` (TEXT) – Poziv na broj
- `is_active` (INTEGER, DEFAULT 1) – Je li aktivan predložak
- `created_at` (TEXT) – Datum kreiranja
- `updated_at` (TEXT) – Datum zadnje promjene

---

#### Newsletter_template (Predlošci newslettera)
Tablica predložaka za email komunikaciju koja omogućuje kreiranje i čuvanje standardiziranih email poruka za različite svrhe (informativni newsletteri, najave radionica), s mogućnošću kategoriziranja prema tipu i aktiviranja/deaktiviranja prema potrebi.

**Polja:**
- `id` (INTEGER, PRIMARY KEY) – Jedinstveni identifikator
- `name` (TEXT, NOT NULL) – Naziv predloška
- `subject` (TEXT, NOT NULL) – Naslov emaila
- `content` (TEXT, NOT NULL) – Sadržaj emaila
- `template_type` (TEXT, DEFAULT 'PAYMENT') – Tip predloška
- `is_active` (INTEGER, DEFAULT 1) – Je li aktivan
- `created_at` (TEXT) – Datum kreiranja
- `updated_at` (TEXT) – Datum zadnje promjene

---

#### Payment_attachment (Predlošci uplatnica)
Tablica HTML predložaka za vizualno oblikovanje uplatnica koje se generiraju u PDF formatu. Sadrži HTML kod s placeholderima koji se dinamički popunjavaju podacima o plaćanju i omogućuje definiranje zadanog predloška za automatsko korištenje.

**Polja:**
- `id` (INTEGER, PRIMARY KEY) – Jedinstveni identifikator
- `name` (TEXT, NOT NULL) – Naziv predloška
- `description` (TEXT) – Opis predloška
- `html_content` (TEXT, NOT NULL) – HTML sadržaj s placeholderima
- `is_default` (INTEGER, DEFAULT 0) – Je li zadani predložak
- `created_at` (TEXT) – Datum kreiranja
- `updated_at` (TEXT) – Datum zadnje promjene

---

### 3.3.3. Ključni odnosi i veze

**Osnovni odnosi:**
- Jedan-na-više: **Organization → Contacts, Teachers, Workshops, Payment_info**
- Jedan-na-više: **Contact → Underaged** (jedan skrbnik, više djece)
- Jedan-na-više: **Teacher → Workshops** (jedan učitelj, više radionica)
- Jedan-na-više: **Workshop → Workshop_participants** (jedna radionica, više polaznika)

**Kompleksni odnosi:**
- Mnogostruki: **Contacts ↔ Workshops** (preko *Workshop_participants*)
- Mnogostruki: **Contacts ↔ Lists** (preko *List_contacts*)

**Polimorfni odnosi:**
- *Workshop_participants*: može referencirati ili **Contact** ili **Underaged**, ovisno o `participant_type`

---

### 3.3.4. Ograničenja integriteta

**Check constrainti:**
- `workshop_participants`: logička provjera `participant_type` i odgovarajućih foreign key-eva
- Enumeratori za sva status polja

**Foreign key constrainti:**
- Sve reference su zaštićene foreign key constraintima
- **CASCADE DELETE** za ovisne podatke
- **SET NULL** za opcijske reference

**Unique constrainti:**
- `list_contacts`: `(list_id, contact_id)` – sprečava duplikate u istoj listi

---

### 3.3.5. Migracije i održavanje

**Automatske migracije:**
- `addTeacherIdToWorkshops()` – Dodaje `teacher_id` kolonu u *workshops* ako ne postoji
- `fixWorkshopParticipantsForTeachers()` – Uklanja zastarjeli `teacher_id` iz *workshop_participants*
- `insertDefaultPaymentTemplate()` – Umeće zadani HTML predložak za uplatnice

**Soft delete funkcionalnost:**
- Tablica *Lists* podržava soft delete s `is_deleted` i `deleted_at` poljima
- Omogućuje vraćanje slučajno obrisanih lista

**Audit trail:**
- Sve tablice imaju `created_at` i `updated_at` polja za praćenje promjena

---

### 3.3.6. Indeksi i performanse

**Preporučeni indeksi:**


-- Za brže pretraživanje kontakata
CREATE INDEX idx_contacts_email ON contacts(email);
CREATE INDEX idx_contacts_member ON contacts(is_member);

-- Za brže povezivanje maloljetnih s skrbnicima
CREATE INDEX idx_underaged_contact ON underaged(contact_id);

-- Za brže dohvaćanje polaznika radionica
CREATE INDEX idx_workshop_participants_workshop ON workshop_participants(workshop_id);
CREATE INDEX idx_workshop_participants_contact ON workshop_participants(contact_id);

-- Za optimizaciju lista
CREATE INDEX idx_list_contacts_list ON list_contacts(list_id);
CREATE INDEX idx_lists_deleted ON lists(is_deleted); 

# 4. ARHITEKTURA SUSTAVA

Aplikacija **„SmallBusinessBuddy“ CRM** koristi Java tehnologije za izradu Desktop aplikacija.  
**JavaFX** je tehnologija koja je odabrana za implementaciju ovog projekta.

---

## 4.1. Glavne tehnologijske komponente

### Eclipse Temurin OpenJDK (Standard Edition)
- **Verzija:** OpenJDK 21.0.7+6-LTS (Eclipse Temurin)  
- **Build:** Temurin-21.0.7+6 (build 21.0.7+6-LTS)  
- **VM:** OpenJDK 64-Bit Server VM (mixed mode, sharing)  
- **Svrha:** Osnovni runtime environment i programski jezik  
- **Razlog odabira:** LTS podrška, moderne značajke jezika, stabilnost, besplatna distribucija  

### JavaFX
- **Verzija:** JavaFX 21  
- **Svrha:** Kreiranje modernog korisničkog sučelja (GUI)  
- **Značajke:** Scene Builder integracija, CSS stiliziranje, FXML layouti  
- **Komponente:** Controls, Charts, WebView, Media  

### SQLite
- **Verzija:** SQLite 3.x  
- **Svrha:** Lokalna baza podataka  
- **Prednosti:**  
  - Nema potrebu za vanjskim serverom  
  - ACID transakcije  
  - Nulta konfiguracija  
  - Portabilnost podataka  

---

## 4.2. Biblioteke i ovisnosti

### iText PDF
- **Svrha:** Generiranje PDF uplatnica s HUB-3 barkodovima  
- **Funkcionalnosti:** PDF kreiranje, barcode generiranje, HTML-to-PDF konverzija  

### JavaMail API
- **Svrha:** Slanje newslettera i email obavijesti  
- **Protokoli:** SMTP, IMAP, POP3  
- **Sigurnost:** TLS/SSL podrška  

### Jackson JSON
- **Svrha:** JSON serijalizacija/deserijalizacija  
- **Upotreba:** Konfiguracije, data export/import  

### Apache Commons
- **Svrha:** Utility funkcije  
- **Moduli:** Commons IO, Commons Lang, Commons Validator  

### ZXing (Zebra Crossing)
- **Svrha:** Generiranje i čitanje barkodova  
- **Podržani formati:** PDF417 (HUB-3), QR Code, Code 128  

---

## 4.3. Razvojni alati

### Maven
- **Svrha:** Build management i dependency management  
- **Konfiguracija:** `pom.xml` s definiranim ovisnostima  
- **Profili:** development, testing, production  

### Git
- **Svrha:** Version control sustav  

---

## 4.4. Struktura aplikacije
Aplikacija je organizirana prema **layered architecture** principima koji omogućavaju jasno razdvajanje odgovornosti i lakše održavanje koda.  

---

## 4.5. Organizacija kontrolera po domenama

### Contact Management (`controllers/contact/`)
- `ContactViewController` – Glavni pregled kontakata  
- `ContactSelectionDialog` – Odabir kontakata  
- `ContactsReportController` – Izvještaji kontakata  
- `CreateContactDialog` – Kreiranje novih kontakata  
- `EditColumnsDialog` – Uređivanje stupaca prikaza  
- `EditContactDialog` – Uređivanje postojećih kontakata  
- `ImportContactsDialog` – Import kontakata iz vanjskih izvora  

### List Management (`controllers/list/`)
- `ListsController` – Upravljanje prilagođenim listama kontakata  
- `ListsReportController` – Izvještaji o listama  

### Organization Management (`controllers/organization/`)
- `OrganizationController` – Upravljanje osnovnim podacima organizacije  
- `OrganizationSetupDialog` – Početno podešavanje organizacije  

### Teacher Management (`controllers/teacher/`)
- `CreateEditTeacherDialog` – Kreiranje i uređivanje učitelja  
- `TeacherManagementDialog` – Upravljanje učiteljima  
- `TeachersViewController` – Pregled svih učitelja  

### Underaged Management (`controllers/underaged/`)
- `BarcodeGeneratorViewController` – Generiranje barkodova za maloljetne  
- `UnderagedMemberController` – Upravljanje maloljetnim polaznicima  
- `UnderagedReportController` – Izvještaji o maloljetnim polaznicima  

### Utilities (`controllers/utilities/`)
- `BarcodePaymentDialog` – Dijalozi za barcode plaćanja  
- `CreatePaymentTemplateDialog` – Kreiranje novih predložaka plaćanja  
- `EditPaymentTemplateDialog` – Uređivanje predložaka plaćanja  
- `GoogleOAuthController` – Google OAuth integracija  
- `MultipleGenerationBarcodeDialog` – Masovno generiranje barkodova  
- `NewsletterBuilderController` – Kreiranje newslettera  
- `NewsletterSendDialog` – Slanje newslettera  
- `PaymentAttachmentController` – HTML predlošci uplatnica  
- `PaymentTemplateViewController` – Upravljanje predlošcima plaćanja  
- `ReportingDashboardController` – Glavni dashboard s pokazateljima  

### Workshop Management (`controllers/workshop/`)
- `CreateWorkshopDialog` – Kreiranje novih radionica  
- `EditWorkshopDialog` – Uređivanje postojećih radionica  
- `WorkshopParticipantsViewController` – Upravljanje polaznicima radionica  
- `WorkshopPaymentSlipsController` – Plaćanja za radionice  
- `WorkshopsReportController` – Izvještaji o radionicama  
- `WorkshopsViewController` – Pregled i upravljanje radionicama  

---

## 4.6. Data Access Layer (`database/`)

### Core DAO komponente
- `DatabaseConnection` – Singleton za upravljanje konekcijama  
- `ContactDAO` – CRUD operacije za kontakte  
- `ListsDAO` – CRUD operacije za liste  
- `NewsletterTemplateDAO` – CRUD operacije za newsletter predloške  
- `OrganizationDAO` – CRUD operacije za organizaciju  
- `PaymentAttachmentDAO` – CRUD operacije za payment attachments  
- `PaymentTemplateDAO` – CRUD operacije za predloške plaćanja  
- `TeacherDAO` – CRUD operacije za učitelje  
- `UnderagedDAO` – CRUD operacije za maloljetne polaznike  
- `WorkshopDAO` – CRUD operacije za radionice  
- `WorkshopParticipantDAO` – CRUD operacije za polaznike radionica  

### Značajke DAO sloja
- Prepared statements za SQL injection zaštitu  
- Transaction management  
- Connection pooling  
- Error handling i logging  

---

## 4.7. Domain Model (`model/`)

### Glavni entiteti
- `Contact` – Punoljetni kontakti/skrbnici  
- `List` – Prilagođene liste kontakata  
- `NewsletterTemplate` – Email predlošci  
- `OAuthToken` – OAuth tokeni  
- `Organization` – Podaci o organizaciji  
- `PaymentAttachment` – HTML predlošci za plaćanja  
- `PaymentTemplate` – Predlošci plaćanja  
- `Teacher` – Učitelji/instruktori  
- `UnderagedMember` – Maloljetni polaznici  
- `Workshop` – Radionice  
- `WorkshopParticipant` – Polaznici radionica  

### Značajke modela
- JavaBean konvencije (getters/setters)  
- Validation anotacije  
- Builder pattern gdje je potreban  
- Immutable objekti za konfiguraciju  

---

## 4.7. Business Logic Layer (`services/`)

### Newsletter servisi
- `NewsletterComponentBuilder` – služi prilikom izrade newsletter komponenti  
- `NewsletterService` – Glavni servis za slanje elektroničke pošte  
- `TemplateManager` – Upravljanje predlošcima  

### Google integracije
- `CallbackServer` – OAuth callback handling  
- `GmailService` – Gmail API integracija  
- `GoogleOAuthManager` – OAuth token management  
- `OAuthService` – Općeniti OAuth servis  
- `TokenManager` – Token lifecycle management  

### Utility servisi
- `BarcodePaymentService` – generiranje barkoda  
- `LanguageManager` – Internacionalizacija  
- `JsonUtils` – JSON operacije  
- `UplatnicaHtmlGenerator` – HTML generiranje za uplatnice  

---

## 4.8. Model-Pogled-Upravljač arhitektura (MVC)

Aplikacija implementira **MVC arhitekturni uzorak** koji omogućuje jasno razdvajanje poslovne logike, korisničkog sučelja i kontrole toka aplikacije.

### 4.8.1. Model sloj
**Odgovornosti:**
- Reprezentacija poslovnih entiteta  
- Validacija podataka i pravila poslovanja  
- Integracija s bazom podataka preko DAO sloja  

### 4.8.2. Pogled sloj
**Odgovornosti:**
- Prikaz korisničkog sučelja  
- Provjera unosa na razini korisničkog sučelja  
- Korisničko iskustvo i odzivnost  
- Stiliziranje i teme  

**Implementacija:**
- FXML datoteke za definiciju rasporeda  
- CSS stilovi za vizualno oblikovanje  
- JavaFX upravljačke klase povezane s FXML  
- Promatrana svojstva za povezivanje podataka  
- Rukovanje događajima za korisničke interakcije  

**Značajke:**
- Prilagodljiv dizajn  
- Internacionalizacija (i18n) – prijevodi na hrvatski i engleski  
- Podrška za pristupačnost  
- Dosljedni uzorci korisničkog iskustva  

### 4.8.3. Upravljač sloj
**Odgovornosti:**
- Koordinacija između sloja pogleda i modela  
- Izvršavanje poslovne logike  
- Navigacija i upravljanje tijekom rada  
- Rukovanje greškama i povratne informacije korisniku  
- Integracija s vanjskim servisima  

**Organizacija upravljača:**
- Grupiranje po domenama  
- Upravljači su organizirani po funkcionalnostima  
- Jasne odgovornosti za svaki upravljač  

**Upravljanje životnim ciklusom:**
- `initialize()` metode za postavljanje  
- Metode za rukovanje događajima  
- Čišćenje u `onClose()` metodama  
- Sprječavanje curenja memorije  

### 4.8.4. Tok podataka i komunikacija
- **Pogled → Upravljač:** rukovanje događajima, povezivanje podataka, validacija  
- **Upravljač → Model:** pozivi servisa, DAO operacije, poslovna logika  
- **Model → Pogled:** promatrana svojstva, povezivanje podataka, obavijesti  

**Integracija sloja servisa:**
- Dependency injection uzorak  
- Lociranje servisa za dijeljene servise  
- Asinkrone operacije za dugotrajne zadatke  

### 4.8.5. Prednosti MVC arhitekture
- **Održivost:** jasne odgovornosti, lako testiranje, modularnost  
- **Skalabilnost:** dodavanje novih funkcionalnosti bez utjecaja na postojeće komponente  
- **Ponovna upotreba:** modeli i servisi dijeljeni među komponentama  
- **Timski rad:** frontend/backend developeri rade paralelno bez konflikata  

---

# 5. ZAKLJUČAK

Razvoj aplikacije **SmallBusinessBuddy - CRM** predstavlja pokušaj integracije modernih tehnologija i prilagođenih rješenja za potrebe **malih poduzeća, obrta i neprofitnih organizacija**, primjerice **Društva "Naša Djeca" Poreč**.

Aplikacija, bazirana na **MVC arhitekturi** uz korištenje **JavaFX-a**, nudi platformu koja podržava:
- osnovno upravljanje klijentima
- automatizaciju generiranja uplatnica
- praćenje radionica i polaznika
- e-mail komunikaciju putem integracije s Gmailom preko **Google OAuth-a**

Moduli poput **upravljanja kontaktima**, **pregleda radionica**, **izvješća o maloljetnim članovima** i **upravljanja predlošcima uplatnica** omogućuju pregled podataka i osnovnu fleksibilnost, uz ograničenu upotrebu vizualizacija i statistika.

Aplikacija ima prostora za poboljšanje, uključujući:
- širu integraciju s vanjskim servisima
- poboljšanje performansi
- razvoj sofisticiranijih alata za analizu

Unatoč ograničenjima, **SmallBusinessBuddy - CRM** pruža funkcionalnu bazu koja može poslužiti kao polazna točka za daljnji razvoj i eventualnu primjenu u različitim sektorima.

Ovaj projekt pokazuje **tehničke kompetencije** i **potencijal za podršku digitalizaciji** malih organizacija, no njegov uspjeh u praksi ovisit će o daljnjim unapređenjima i prilagodbi stvarnim korisničkim potrebama.  
