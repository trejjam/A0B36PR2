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

<a rel="license" href="http://creativecommons.org/licenses/by-nc-sa/3.0/deed.cs"><img alt="Licence Creative Commons" style="border-width:0" src="http://i.creativecommons.org/l/by-nc-sa/3.0/88x31.png" /></a><br /><span xmlns:dct="http://purl.org/dc/terms/" property="dct:title">LBoat</span>, jejímž autorem je <span xmlns:cc="http://creativecommons.org/ns#" property="cc:attributionName">Jan Trejbal</span>,
<br />podléhá licenci <a rel="license" href="http://creativecommons.org/licenses/by-nc-sa/3.0/deed.cs">Creative Commons Uveďte autora-Nevyužívejte dílo komerčně-Zachovejte licenci 3.0 Unported </a>.
