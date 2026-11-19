package com.example.shopexercice
import java.io.File


interface Remisable {
    fun appliquerRemise(pourcentage: Double)
}
abstract class Produit(
    val code: String,
    val nom: String,
    var prixHT: Double,
          stock: Int
) {
    var stock: Int = stock
        private set

    abstract fun prixTTC(): Double

    fun retirerStock(quantite: Int): Boolean {
        if (quantite <= 0 || quantite > stock) {
            return false
        }

        stock -= quantite
        return true
    }

    fun ajouterStock(quantite: Int) {
        if (quantite > 0) {
            stock += quantite
        }
    }

}

class ProduitElectronique(
    code: String,
    nom: String,
    prixHT: Double,
    stock: Int,
    val garantieMois: Int
) : Produit(code, nom, prixHT, stock), Remisable {

    override fun prixTTC(): Double {
        return prixHT * 1.20
    }
    override fun appliquerRemise(pourcentage: Double) {
        prixHT -= prixHT * pourcentage / 100
    }
}

class ProduitAlimentaire(
    code: String,
    nom: String,
    prixHT: Double,
    stock: Int,
    val dateLimite: String
) : Produit(code, nom, prixHT, stock) {

    override fun prixTTC(): Double {
        return prixHT * 1.10
    }
}
data class Client(
    val id: Int,
    val nom: String,
    val ville: String
)
class Commande(
    val numero: Int,
    val date: String,
    val client: Client,
    val produit: Produit,
    val quantite: Int
) {
    var statut:StatutCommande = StatutCommande.CREATED
        private set
    fun montantTotal(): Double {
        return produit.prixTTC() * quantite
    }

    fun payer(): Boolean {
        if (statut != StatutCommande.CREATED) {
            return false
        }

        if (!produit.retirerStock(quantite)) {
            return false
        }

        statut = StatutCommande.PAID
        return true
    }

    fun expedier(): Boolean {
        if (statut != StatutCommande.PAID) {
            return false
        }

        statut = StatutCommande.SHIPPED
        return true
    }

    fun annuler(): Boolean {
        if (statut == StatutCommande.SHIPPED || statut == StatutCommande.CANCELLED) {
            return false
        }

        if (statut == StatutCommande.PAID) {
            produit.ajouterStock(quantite)
        }

        statut = StatutCommande.CANCELLED
        return true
    }

}
enum class StatutCommande {
    CREATED,
    PAID,
    SHIPPED,
    CANCELLED
}


fun ecrireHistorique(
    client: Client,
    commandes: List<Commande>
) {
    val texte = commandes
        .filter { it.client.id == client.id }
        .joinToString("\n") {
            "${it.numero}; ${it.date}; ${it.produit.nom}; ${it.quantite}; ${it.montantTotal()}; ${it.statut}"
        }

    File("Client${client.id}.txt").writeText(texte)
}

class Boutique {

    private val produits = mutableListOf<Produit>()

    fun indiceProduit(code: String): Int {
        return produits.indexOfFirst { it.code == code }
    }

    fun ajouter(p: Produit): Boolean {
        if (indiceProduit(p.code) != -1) {
            return false
        }

        produits.add(p)
        return true
    }

    fun supprimer(code: String): Boolean {
        val indice = indiceProduit(code)

        if (indice == -1) {
            return false
        }

        produits.removeAt(indice)
        return true
    }

    fun supprimer(p: Produit): Boolean {
        return supprimer(p.code)
    }

    fun nombreProduitsElectroniques(): Int {
        return produits.count { it is ProduitElectronique }
    }
}



fun main() {
    val laptop = ProduitElectronique(
        "E1", "Laptop", 5000.0, 10, 24
    )

    val riz = ProduitAlimentaire(
        "A1", "Riz", 20.0, 100, "2027-01-01"
    )

    println(laptop.prixTTC())
    println(riz.prixTTC())

    val catalogue: List<Produit> = listOf(laptop, riz)

    for (p in catalogue) {
        println("${p.nom} : ${p.prixTTC()}")
    }
    laptop.appliquerRemise(10.0)
    println(laptop.prixHT)
    println(laptop.prixTTC())

    println(laptop.retirerStock(3))
    println(laptop.stock)

    println(laptop.retirerStock(50))
    println(laptop.stock)

    laptop.ajouterStock(3)
    println(laptop.stock)
    val sara = Client(1, "Sara", "Marrakech")
    val amine = Client(2, "Amine", "Safi")
    val rania = Client(3, "Rania", "Agadir")
    val phone = ProduitElectronique(
        "E2", "Phone", 2000.0, 1, 12
    )

    val c1 = Commande(1, "2026-10-10", sara, laptop, 2)
    val c2 = Commande(2, "2026-10-11", amine, phone, 5)
    val c3 = Commande(3, "2026-10-12", rania, riz, 3)
    val c4 = Commande(4, "2026-10-13", sara, phone, 1)
    val c5 = Commande(5, "2026-10-14", amine, riz, 10)
    val c6 = Commande(6, "2026-10-15", rania, laptop, 1)

    println(c1.payer())
    println(laptop.stock)
    println(c1.expedier())
    println(c1.annuler())

    println(c2.payer())
    println(phone.stock)

    println(c3.payer())
    println(riz.stock)
    println(c3.annuler())
    println(riz.stock)

    println(c4.payer())
    println(phone.stock)

    println(c5.annuler())
    println(riz.stock)

    println(c6.payer())
    println(laptop.stock)
}

