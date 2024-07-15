# progetto-dispositivi-mobili
GRUPPO: Hack&Horizon

Membri: 
Oleksandra Golub - 856706
Alessandro redaelli - 869841
Taha Mounji - 780471



IN SINTESI:

registrazione password/login e via google 

login password/login e via google

password dimenticata

dopo login:
- se sei USER vai in main_user con la gestione degli fragment da user (non si puo aggiungere cataloghi/collezioni, non si puo aggiungere comics e modificarli); 
- se sei ADMIN vai in main_admin con la gestione degli fragment da admin (si puo aggiungere cataloghi/collezioni, si puo aggiungere comics e modificarli); 

parte profile ti permette di modificare i tuoi dati dell'account, aggiungere/modificare l'immagine di profilo, modificare login e password 
+ se sei registrato tramite logan/passsword eliminare account 
(+ si puo' vedere le pagine dell'account degli altri utenti e loro preferiti comics cliccando sul cuore in alto a destra)

parte chat ti permette di scambiare i messaggi tra gli utenti 

parte ricerca comune ti permette di cercare i comics sia prelevati tramite api che inseriti manualmente in base al nome 

parte ricerca avanzata ti permette di cercare i comics in base a lingua (spinner), anno (spinner), categoria/collezione (multispinner), genere (multispinner)

parte home ti permette vedere i 10 comics piu visualizzati, piu scaricati, per ogni catalogo/collezione (con la possibilita' di aggiungere nuovi 10 comics sotto ulteriormente e ancora, acnora, ancora finche' non si esauriscono)

parte comics ti permette di vedere i dettagli del comics selezionato, lasciare il commento + leggere il comics, scaricarlo o aggiungere ai tuoi preferiti 

parte categorie(sono inseriti manualmente)/collezioni(sono prelevati tramite api) ti permette di vedere i comics in base a categoria/collezione scelta:
- una volta scelta categoria (inserita manualmente) visualizza comics inseriti manualmente di questa categoria;
- una volta scelta collezione (prelevata tramite api) inizialmente visualizza comics inseriti manualmente di questa collezione, se si schiccia su tasto "Clicca qua!" appaiono comics prelevati tramite api di questa collezione; 
da notare: categorie/comics si distinguono in base al tipo dell'utente(USER o ADMIN) -> se sei admin, allora puoi aggiungere nuove categorie, caricare nuovi comics, modificare ed eliminare vecchi comics, altrimenti non si puo fare

NOTA:
è stato lascito il packege characters_api_marvel_prova come una cosa di divertimento + dimostrazione che sappiamo usare anche altri api ufficiali come api di marvel
questa parte visualizza i dati di un personaggio esistente di marvel (https://developer.marvel.com/)

tipico esempio: iron man

ovviamente ci sono cose da migliorare ancora 
- gestire la parte in cui essendo admin scrivi i messaggi agli user senza darli la possibilita' di rispondere (quindi loro possono solo leggere il tuo messaggio) -> quindi sarebbe un'ínvio automatico del messaggio a tutti utenti user;
- aggiungere liste di comics (simile a parte di categorie/collezioni per user e admin) in base ai generi/anni/lingue;
