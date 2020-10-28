package se325.assignment01.concert.service.domain;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * The Concert class models that of a real life concert. It contains fields such as a generated ID, the title of the
 * concert, the preview image, a short blurb describing the concert, a list of performers in the concert, and the
 * dates for which the concert takes place on
 */
@Entity
@Table(name = "CONCERTS")
public class Concert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id; //Database generated ID to uniquely identify concert

    @Column(name = "TITLE")
    private String title; //The title of the concert

    @Column(name = "IMAGE_NAME")
    private String imageName; //The preview image of the concert

    @Column(name = "BLURB", length = 1024)
    private String blurb; //A description of the concert

    @ManyToMany(cascade = CascadeType.PERSIST) //Cascade the persistence to all the performers if a concert is persisted
    @Fetch(FetchMode.SUBSELECT) //Subselect mode to optimise the retrieval of the performer list if a concert is retrieved
    @JoinTable(name = "CONCERT_PERFORMER", joinColumns = @JoinColumn(name = "CONCERT_ID"), inverseJoinColumns = @JoinColumn(name = "PERFORMER_ID"))
    @Column(name = "PERFORMER")
    private Set<Performer> performers; //The list of performers performing in the concert

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "CONCERT_DATES", joinColumns = @JoinColumn(name = "CONCERT_ID"))
    @Column(name = "DATE")
    private Set<LocalDateTime> dates; //The dates for which the concert is held on

    public Concert() {

    }

    public Concert(Long id, String title, String imageName, String blurb, Set<Performer> performers) {
        this.id = id;
        this.title = title;
        this.imageName = imageName;
        this.blurb = blurb;
        this.performers = performers;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getBlurb() {
        return blurb;
    }

    public void setBlurb(String blurb) {
        this.blurb = blurb;
    }

    public Set<Performer> getPerformers() {
        return performers;
    }

    public void setPerformers(Set<Performer> performers) {
        this.performers = performers;
    }

    public Set<LocalDateTime> getDates() {
        return dates;
    }

    public void setDates(Set<LocalDateTime> dates) {
        this.dates = dates;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Concert))
            return false;

        if (obj == this)
            return true;

        Concert rhs = (Concert) obj;
        return new EqualsBuilder().
                append(title, rhs.title).
                isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).
                append(title).hashCode();
    }
}
