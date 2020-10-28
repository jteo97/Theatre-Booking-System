package se325.assignment01.concert.service.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import se325.assignment01.concert.common.types.Genre;

import javax.persistence.*;
import java.util.Set;

/**
 * The Performer class models that of a performer. It contains fields such as a generated ID, the name of the performer,
 * the photo of the performer, the genre that the performer acts in, a short blurb describing the performer, and the list
 * of concerts that the performer acts in
 */
@Entity
@Table(name = "PERFORMERS")
public class Performer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id; //Database generated ID to uniquely identify concert

    @Column(name = "NAME")
    private String name; //The name of the performer

    @Column(name = "IMAGE_NAME")
    private String imageName; //The photo of the performer

    @Column(name = "GENRE")
    @Enumerated(EnumType.STRING) //To show the string representation of the genre e.g. "HipHop"
    private Genre genre; //The genre the performer acts in

    @Column(name = "BLURB", length = 1024)
    private String blurb; //A description of the performer

    @ManyToMany(cascade = CascadeType.PERSIST)
    private Set<Concert> concerts; //The list of concerts the performer acts in

    public Performer() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public String getBlurb() {
        return blurb;
    }

    public void setBlurb(String blurb) {
        this.blurb = blurb;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Performer, id: ");
        buffer.append(id);
        buffer.append(", name: ");
        buffer.append(name);
        buffer.append(", s3 image: ");
        buffer.append(imageName);
        buffer.append(", genre: ");
        buffer.append(genre.toString());

        return buffer.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Performer))
            return false;

        if (obj == this)
            return true;

        Performer rhs = (Performer) obj;
        return new EqualsBuilder().
                append(name, rhs.name).
                isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).
                append(name).hashCode();
    }
}
