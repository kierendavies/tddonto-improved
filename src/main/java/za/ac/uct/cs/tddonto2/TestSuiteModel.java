package za.ac.uct.cs.tddonto2;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class TestSuiteModel extends AbstractTableModel {
    private List<OWLAxiom> axioms;
    private List<TestResult> results;
    private TestResult preconditionsResult;

    public TestSuiteModel() {
        axioms = new ArrayList<>();
        results = new ArrayList<>();
        preconditionsResult = null;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "Axiom";
            case 1:
                return "Result";
            default:
                throw new IndexOutOfBoundsException("Column index must be less than 2");
        }
    }

    @Override
    public int getRowCount() {
        return axioms.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return axioms.get(rowIndex);
            case 1:
                return results.get(rowIndex);
            default:
                throw new IndexOutOfBoundsException("Column index must be less than 2");
        }
    }

    public void add(OWLAxiom axiom) {
        axioms.add(axiom);
        results.add(null);
    }

    public void remove(int index) {
        axioms.remove(index);
        results.remove(index);
        fireTableDataChanged();
    }

    public void remove(int[] indices) {
        for (int i : indices) {
            axioms.remove(i);
            results.remove(i);
        }
        fireTableDataChanged();
    }

    private void evaluate(AxiomTester tester, int index) {
        TestResult result = tester.test(axioms.get(index));
        results.set(index, result);
    }

    public void evaluateAll(AxiomTester tester) {
        preconditionsResult = tester.testPreconditions();
        if (preconditionsResult != null) {
            for (int i = 0; i < axioms.size(); i++) {
                results.set(i, preconditionsResult);
            }
        } else {
            for (int i = 0; i < axioms.size(); i++) {
                evaluate(tester, i);
            }
        }
        fireTableRowsUpdated(0, axioms.size());
    }

    public void evaluateOnly(AxiomTester tester, int[] indices) {
        preconditionsResult = tester.testPreconditions();
        if (preconditionsResult != null) {
            for (int i : indices) {
                results.set(i, preconditionsResult);
            }
        } else {
            for (int i : indices) {
                evaluate(tester, i);
            }
        }
        fireTableRowsUpdated(0, axioms.size());  // TODO: this range could be smaller
    }

    public void addToOntology(OWLModelManager modelManager, int index) {
        modelManager.applyChange(new AddAxiom(modelManager.getActiveOntology(), axioms.get(index)));
    }
}
