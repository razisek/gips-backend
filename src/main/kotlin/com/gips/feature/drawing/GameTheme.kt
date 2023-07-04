package com.gips.feature.drawing


import kotlin.random.Random

enum class GameTheme(val theme: String) {
    ANIMAL("Hewan"),
    FRUIT("Buah-buahan");

    fun toList(): List<String> {
        return values().map {
            it.toString()
        }
    }

    companion object {
        fun fromString(theme: String): GameTheme {
            return values().first {
                it.theme == theme
            }
        }

        fun fromStringIndex(theme: String): Int {
            return values().indexOfFirst {
                it.theme == theme
            }
        }
    }

}

class Question(val theme: String, val questions: List<String>){
     fun getRandomAnimal() : String{
          var randomIndex = Random.nextInt(questions.size)
          randomIndex = Random.nextInt(questions.size)
          randomIndex = Random.nextInt(questions.size)
          return  questions[randomIndex]
     }
}


val questionList = listOf(Question(
    "Hewan",
    listOf(
        "Singa",
        "Harimau",
        "Gajah",
        "Jerapah",
        "Beruang",
        "Serigala",
        "Kucing",
        "Kuda",
        "Kambing",
        "Sapi",
        "Kerbau",
        "Kanguru",
        "Koala",
        "Kumbang",
        "Katak",
        "Kadal",
        "Kepiting",
        "Anjing",
        "Ayam",
        "Babi",
        "Badak",
        "Banteng",
        "Bebek",
        "Cacing",
        "Cicak",
        "Elang",
        "Gagak",
        "Hamster",
        "Jangkrik",
        "Kadal",
        "Lalat",
        "Lele",
        "Landak",
        "Panda",
        "Rusa",
        "Tapir",
        "Ular",
        "Zebra"        
    )
),
Question(
    "Buah-buahan",
    listOf(
        "Blackberry",
        "Blewah",
        "Blewah",
        "Cokelat",
        "Delima",
        "Durian",
        "Jambu",
        "Jeruk",
        "Kelapa",
        "Kemiri",
        "Kesemek",
        "Kiwi",
        "Kurma",
        "Langsat",
        "Leci",
        "Maja",
        "Mangga",
        "Melon",
        "Naga",
        "Nanas",
        "Pepaya",
        "Pisang",
        "Rambutan",
        "Salak",
        "Sawo",
        "Semangka",
        "Strawberry",
        "Tomat",
        "Anggur",
        "Apel",
        "Belimbing",
        "Rambutan",
        "Salak",
        "Sawo",
        "Ubi"
    )
)
)

// val animalList = Question(
//     "Animal",
//     listOf(
//         "Singa",
//         "Harimau",
//         "Gajah",
//         "Jerapah",
//         "Beruang",
//         "Serigala",
//         "Kucing",
//         "Kuda",
//         "Kambing",
//         "Sapi",
//         "Kerbau",
//         "Kanguru",
//         "Koala",
//         "Kumbang",
//         "Katak",
//         "Kadal",
//         "Kepiting",
//         "Anjing",
//         "Ayam",
//         "Babi",
//         "Badak",
//         "Banteng",
//         "Bebek",
//         "Cacing",
//         "Cicak",
//         "Elang",
//         "Gagak",
//         "Hamster",
//         "Jangkrik",
//         "Kadal",
//         "Lalat",
//         "Lele",
//         "Landak",
//         "Panda",
//         "Rusa",
//         "Tapir",
//         "Ular",
//         "Zebra"        
//     )
// )


// val fruitList = Question(
//     "Fruit",
//     listOf(
//         "Blackberry",
//         "Blewah",
//         "Blewah",
//         "Cokelat",
//         "Delima",
//         "Durian",
//         "Jambu",
//         "Jeruk",
//         "Kelapa",
//         "Kemiri",
//         "Kesemek",
//         "Kiwi",
//         "Kurma",
//         "Langsat",
//         "Leci",
//         "Maja",
//         "Mangga",
//         "Melon",
//         "Naga",
//         "Nanas",
//         "Pepaya",
//         "Pisang",
//         "Rambutan",
//         "Salak",
//         "Sawo",
//         "Semangka",
//         "Strawberry",
//         "Tomat",
//         "Anggur",
//         "Apel",
//         "Belimbing",
//         "Rambutan",
//         "Salak",
//         "Sawo",
//         "Ubi"
//     )
// )

