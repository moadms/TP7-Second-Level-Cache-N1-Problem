package ma.enset.tp7.model;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "t_salles_pedagogiques")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SallePedagogique implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_salle")
    private Long id;

    @Column(name = "code_salle", unique = true, nullable = false, length = 30)
    private String codeSalle;

    @Column(name = "capacite_places")
    private Integer capacite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_departement")
    private DepartementCampus departementRattache;

    public SallePedagogique() {
    }

    public SallePedagogique(String codeSalle, Integer capacite) {
        this.codeSalle = codeSalle;
        this.capacite = capacite;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodeSalle() {
        return codeSalle;
    }

    public void setCodeSalle(String codeSalle) {
        this.codeSalle = codeSalle;
    }

    public Integer getCapacite() {
        return capacite;
    }

    public void setCapacite(Integer capacite) {
        this.capacite = capacite;
    }

    public DepartementCampus getDepartementRattache() {
        return departementRattache;
    }

    public void setDepartementRattache(DepartementCampus departementRattache) {
        this.departementRattache = departementRattache;
    }

    @Override
    public String toString() {
        return "SallePedagogique{" +
                "id=" + id +
                ", codeSalle='" + codeSalle + '\'' +
                ", capacite=" + capacite +
                '}';
    }
}
