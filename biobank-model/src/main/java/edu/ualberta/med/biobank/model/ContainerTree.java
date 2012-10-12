package edu.ualberta.med.biobank.model;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.envers.Audited;

import edu.ualberta.med.biobank.model.type.Decimal;

@Audited
@Entity
@Table(name = "CONTAINER_TREE")
public class ContainerTree
    extends VersionedLongIdModel {
    private static final long serialVersionUID = 1L;

    private CenterLocation location;
    private Center owningCenter;
    private Decimal temperature;

    /**
     * @return the current location of all the {@link Container}s in this
     *         {@link ContainerTree}, or null if there is no location.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CENTER_LOCATION_ID", nullable = false)
    public CenterLocation getLocation() {
        return location;
    }

    public void setLocation(CenterLocation location) {
        this.location = location;
    }

    @NotNull(message = "{ContainerTree.owningCenter.NotNull}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OWNING_CENTER_ID", nullable = false)
    public Center getOwningCenter() {
        return owningCenter;
    }

    public void setOwningCenter(Center owningCenter) {
        this.owningCenter = owningCenter;
    }

    @Valid
    @NotNull(message = "{ContainerTree.temperature.NotNull}")
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "TEMPERATURE_VALUE")),
        @AttributeOverride(name = "scale", column = @Column(name = "TEMPERATURE_SCALE"))
    })
    public Decimal getTemperature() {
        return temperature;
    }

    public void setTemperature(Decimal temperature) {
        this.temperature = temperature;
    }
}
