package ma.enset.tp7.model;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "t_departements_campus")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@NamedEntityGraph(
        name = "Departement.avecSalles",
        attributeNodes = @NamedAttributeNode("salles")
)
public class DepartementCampus implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_dept")
    private Long id;

    @Column(name = "code_dept", unique = true, nullable = false, length = 30)
    private String code;

    @Column(name = "intitule_dept", nullable = false, length = 100)
    private String intitule;

    @OneToMany(mappedBy = "departementRattache", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<SallePedagogique> salles = new ArrayList<>();

    public DepartementCampus() {
    }

    public DepartementCampus(String code, String intitule) {
        this.code = code;
        this.intitule = intitule;
    }

    public void ajouterSalle(SallePedagogique salle) {
        this.salles.add(salle);
        salle.setDepartementRattache(this);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getIntitule() {
        return intitule;
    }

    public void setIntitule(String intitule) {
        this.intitule = intitule;
    }

    public List<SallePedagogique> getSalles() {
        return salles;
    }

    public void setSalles(List<SallePedagogique> salles) {
        this.salles = salles;
    }

    @Override
    public String toString() {
        return "DepartementCampus{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", intitule='" + intitule + '\'' +
                '}';
    }
}
