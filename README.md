A0B36PR2
========

Semestrální práce do předmětu A0B36PR2

Ovládací SW pro WiFi HW - specializovaný pro plachetnici
viz: http://pslib.cz/jan.trejbal/DMP

HW ukázán na cvičení. 21.3.2013

HW obsahuje modul od ConnectOne Mini Socket iWiFi™, MCU ATmega128A.
Podporuje periferie:
  serva 3x
  kompas
  kamera
  náklonoměr
  rychloměr
  
Aplikace bude obsahovat grafické rozhraní, ummožňující připojit se přes WiFi k HW.
Pro komunikaci bude využit protokol TCP/IP (serial over TCP/IP)
Aplikace bude umět ovládat a číst periferie.

Cílem je navrhnout komunikační protokol (implementovat ho v MCU -> rutiny procesoru jsou již napsány z dřívějška)
Vytvořit vhodné grafické rozhraní pro ovládání.
Připravit vše pro možnost snadné implementace autopilota (cílový subjekt vyžaduje projekt v takovémto stavu).
