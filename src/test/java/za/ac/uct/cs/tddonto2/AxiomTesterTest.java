package za.ac.uct.cs.tddonto2;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AxiomTesterTest {
    private static final String pizzaPath = "src/test/resources/pizza.owl";
    private static final String pizzaPrefix = "http://www.co-ode.org/ontologies/pizza/pizza.owl#";

    private static OWLDataFactory dataFactory;
    private static AxiomTester axiomTester;

    private OWLClass parseClass(String name) {
        return dataFactory.getOWLClass(IRI.create(pizzaPrefix, name));
    }

    private OWLNamedIndividual parseIndiv(String name) {
        return dataFactory.getOWLNamedIndividual(IRI.create(pizzaPrefix, name));
    }

    private OWLObjectProperty parseObjProp(String name) {
        return dataFactory.getOWLObjectProperty(IRI.create(pizzaPrefix, name));
    }

    @BeforeAll
    public static void setUp() throws Exception {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        dataFactory = manager.getOWLDataFactory();
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(pizzaPath));
        OWLReasoner reasoner = new ReasonerFactory().createNonBufferingReasoner(ontology);

        // Ensure preconditions
        assertTrue(reasoner.isConsistent());
        assertEquals(1, reasoner.getUnsatisfiableClasses().getSize());  // always includes owl:Nothing

        axiomTester = new AxiomTester(reasoner);
    }

    @Test
    public void testDispatch() throws Exception {
        assertEquals(
                TestResult.ENTAILED,
                axiomTester.test(
                        dataFactory.getOWLSubClassOfAxiom(
                                parseClass("Margherita"),
                                parseClass("Pizza")
                        )
                )
        );
        assertEquals(
                TestResult.MISSING_ENTITY,
                axiomTester.test(
                        dataFactory.getOWLSubClassOfAxiom(
                                parseClass("NotARealPizza"),
                                parseClass("Pizza")
                        )
                )
        );
    }

    @Test
    public void testSubClassOf() throws Exception {
        assertEquals(
                TestResult.ENTAILED,
                axiomTester.testSubClassOf(
                        parseClass("Margherita"),
                        parseClass("NamedPizza")
                )
        );
        assertEquals(
                TestResult.ENTAILED,
                axiomTester.testSubClassOf(
                        dataFactory.getOWLObjectSomeValuesFrom(parseObjProp("hasTopping"), parseClass("MeatTopping")),
                        dataFactory.getOWLObjectComplementOf(parseClass("VegetarianPizza"))
                )
        );
        assertEquals(
                TestResult.ABSENT,
                axiomTester.testSubClassOf(
                        parseClass("Pizza"),
                        parseClass("NamedPizza")
                )
        );
        assertEquals(
                TestResult.INCOHERENT,
                axiomTester.testSubClassOf(
                        parseClass("PizzaBase"),
                        parseClass("PizzaTopping")
                )
        );
        assertEquals(
                TestResult.INCONSISTENT,
                axiomTester.testSubClassOf(
                        parseClass("Country"),
                        dataFactory.getOWLObjectComplementOf(parseClass("Country"))
                )
        );
    }

    @Test
    public void testEquivalentClasses() throws Exception {
        assertEquals(
                TestResult.ENTAILED,
                axiomTester.testEquivalentClasses(
                        parseClass("SpicyPizza"),
                        parseClass("SpicyPizzaEquivalent")
                )
        );
        assertEquals(
                TestResult.ABSENT,
                axiomTester.testEquivalentClasses(
                        parseClass("Country"),
                        parseClass("Pizza")
                )
        );
        assertEquals(
                TestResult.INCOHERENT,
                axiomTester.testEquivalentClasses(
                        parseClass("Country"),
                        parseClass("Pizza"),
                        parseClass("PizzaBase"),
                        parseClass("PizzaTopping")
                )
        );
    }

    @Test
    public void testDisjointClasses() throws Exception {
        assertEquals(
                TestResult.ENTAILED,
                axiomTester.testDisjointClasses(
                        parseClass("Pizza"),
                        parseClass("PizzaBase"),
                        parseClass("PizzaTopping")
                )
        );
        assertEquals(
                TestResult.ABSENT,
                axiomTester.testDisjointClasses(
                        parseClass("SpicyPizza"),
                        parseClass("VegetarianPizza")
                )
        );
        assertEquals(
                TestResult.INCOHERENT,
                axiomTester.testDisjointClasses(
                        parseClass("Pizza"),
                        parseClass("Pizza")
                )
        );
    }

    @Test
    public void testDisjointUnion() throws Exception {
        assertEquals(
                TestResult.ENTAILED,
                axiomTester.testDisjointUnion(
                        parseClass("Food"),
                        parseClass("Pizza"),
                        dataFactory.getOWLObjectIntersectionOf(parseClass("Food"), dataFactory.getOWLObjectComplementOf(parseClass("Pizza")))
                )
        );
        assertEquals(
                TestResult.ABSENT,
                axiomTester.testDisjointUnion(
                        parseClass("Food"),
                        parseClass("Pizza"),
                        parseClass("PizzaBase"),
                        parseClass("PizzaTopping")
                )
        );
        assertEquals(
                TestResult.INCOHERENT,
                axiomTester.testDisjointUnion(
                        parseClass("Food"),
                        parseClass("Pizza"),
                        parseClass("PizzaBase")
                )
        );
    }

    @Test
    public void testSameIndividual() throws Exception {
        assertEquals(
                TestResult.ENTAILED,
                axiomTester.testSameIndividual(
                        parseIndiv("England"),
                        parseIndiv("England")
                )
        );
        assertEquals(
                TestResult.ABSENT,
                axiomTester.testSameIndividual(
                        parseIndiv("England"),
                        parseIndiv("Scotland")
                )
        );
        assertEquals(
                TestResult.INCONSISTENT,
                axiomTester.testSameIndividual(
                        parseIndiv("England"),
                        parseIndiv("France")
                )
        );
    }

    @Test
    public void testDifferentIndividuals() throws Exception {
        assertEquals(
                TestResult.ENTAILED,
                axiomTester.testDifferentIndividuals(
                        parseIndiv("England"),
                        parseIndiv("France")
                )
        );
        assertEquals(
                TestResult.ABSENT,
                axiomTester.testDifferentIndividuals(
                        parseIndiv("England"),
                        parseIndiv("Scotland")
                )
        );
        assertEquals(
                TestResult.INCONSISTENT,
                axiomTester.testDifferentIndividuals(
                        parseIndiv("England"),
                        parseIndiv("England")
                )
        );
    }

    @Test
    public void testClassAssertion() throws Exception {
        assertEquals(
                TestResult.ENTAILED,
                axiomTester.testClassAssertion(
                        parseClass("Country"),
                        parseIndiv("England")
                )
        );
        assertEquals(
                TestResult.ABSENT,
                axiomTester.testClassAssertion(
                        parseClass("Food"),
                        parseIndiv("England")
                )
        );
        assertEquals(
                TestResult.INCONSISTENT,
                axiomTester.testClassAssertion(
                        dataFactory.getOWLObjectComplementOf(parseClass("Country")),
                        parseIndiv("England")
                )
        );
    }
}