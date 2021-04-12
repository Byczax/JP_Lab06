# Laboratorium 6, TN 11:00

Podczas laboratorium należy zbudować aplikację działającą w środowisku rozproszonym, wykorzystującą do komunikacji gniazda TCP/IP (klasy ServerSocket, Socket). Dokładniej - należy zaimlementować mały systemu, w którego skład wejdą instancje klas uruchamiane równolegle (na jednym lub na kilku różnych komputerach).
Specyfikacja problemu:

* budowane rozwiązanie ma być symulatorem systemu do zarządzania pokojami hotelowymi;
* w systemie tym mają istnieć elementy: Hotel (hotel, jedna instancja), Room (pokój, wiele instancji), Terminal (terminal gościa, wiele instancji);
* Hotel ma pokoje jedno, dwu i trzy osobowe. Liczba pokoi każdego typu ma być parametryzowana. Stan pokoi powinien być widoczny na graficznym interfejsie. Hotel ma gniazdo serwerowe działające na znanym hoście i zadanym porcie (numer portu można wpisać na graficznym interfejsie).
  * Hotel:
  * przyjmuje żądania od pokoi o przyznanie numeru oraz portu (zawierające informację o typie pokoju). Żądanie te wysyłane są, gdy pokoje startują (gdy pokoje są "włączane" do systemu hotelowego). W odpowiedzi przesyła numer portu, na jakim dany pokój otworzyć ma gniazdo serwerowe. Przy okazji uzupełnia własną listę dostępnych pokoi. Tworzy też gniazda klienckie do komunikacji z gniazdami serwerowymi pokoi.
  * przyjmuje żądania od pokoi o ich usunięcie z systemu hotelowego. Reaguje na nie usunięciem danego pokoju z listy dostępnych pokoi i wysłaniem potwierdzenia tej czynności (hotel nie musi sprawdzać, czy pokój jest zajęty czy wolny).
  * przyjmuje żądania dokonania rezerwacji przychodzące z terminali gości, zawierające informację o nazwie gościa, liczbie i typach rezerwowanych pokoi (dla uproszczenia pomijamy terminy rezerwacji). W odpowiedzi do każdego takiego żądania wysyła informację o dokonanej rezerwacji, zawierającą numery i klucze zarezerwowanych pokoi, host i porty, na których działają pokoje. Jeśli rezerwacji nie można dokonać, w odpowiedzi przesyła informaję o odrzuceniu rezerwacji.
  * przyjmuje indywidualne żądania zakończenia pobytu od terminali gości, zawierające numery i klucze pokoi. W odpowiedzi przesyła potwierdzenie zakończenia pobytu (i resetuje klucze pokoi) pod warunkiem, że pokoje zostały zwolnione. W przeciwnym wypadku zwraca informację, że zakończenie pobytu nie jest możliwe z powodu zajmowania pokoi o podanych numerach
  * przyjmuje żądania od pokoi zawierające informacje o ich stanie (patrz opis pokoju i klienta).
* Room reprezentuje pokój o zadanego typu (jedno, dwu lub trzyosobowy). Oferuje graficzny interfejs (by pokazać swój numer i stan). Pokój wie, na jakim hoście i porcie działa hotel. Pokój tworzy gniazdo klienckie do komunikacji z gniazdem serwerowym hotelu. Tworzy też własne gniazdo serwerowe, na którym przyjmuje żądania od hotelu oraz terminali klientów.
  * Room:
  * zwraca się do hotelu o przyznanie numeru oraz portu wysyłając odpowiednio zredagowane żądanie (zakładamy, że room utworzy gniazdo serwerowe na tym samym hoście co hotel, z numerem portu pozyskanym z hotelu).
  * przyjmuje od hotelu żądania aktualizacji klucza.
  * informuje hotel o swoim stanie, wysyłając odpowiednio zredagowane żądanie. Stan pokoju zmienia się na skutek obsługi żadania przychodzącego z terminala klienta.
  * informuje hotel o swoim "wyłączeniu" z systemu hotelowego (żądanie to może być wysłane np. podczas zamykania instancji pokoju).
  * przyjmuje żadania z terminali gości, zawierające informacje o numerze pokoju oraz kluczu (parametry te muszą się zgadzać z przypisanym do pokoju numerem i kluczem, inaczej powinno nastąpić odrzucenie żądania) oraz informacje o stanie (jeśli gość "wchodzi" do pokoju, to zmienia stan pokoju na "zajęty", jeśli "wychodzi" z pokoju, to zmienia stan pokoju na "wolny".
* Terminal gościa reprezentuje aplikację, za pomocą której gość może zarezerwować pokój w hotelu, a później z niego korzystać. Terminal gościa wie, na jakim hoście i porcie działa hotel. Informacje o tym, na jakich portach działają pokoje otrzymuje w odpowiedzi na żądanie rezerwacji.\
Terminal:
  * oferuje graficzny interfejs, za pomocą którego gość może wysyłać żądania rezerwacji pokoi oraz żądanie zakończenia pobytu.
  * wyświetla listy zarezerwowanych przez gościa pokoi i ich stany.
  * umożliwia "wejście" i "wyjście" z danego pokoju poprzez przesłanie odpowiednio zredagowanego żądania do odpowiedniego pokoju.

Pozostałe szczegóły mają być zgodne z ustaleniami poczynionymi na początku zajęć. 
