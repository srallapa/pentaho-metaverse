package com.pentaho.metaverse.analyzer.kettle.jobentry;

import com.pentaho.metaverse.analyzer.kettle.BaseKettleMetaverseComponent;
import org.pentaho.di.job.entry.JobEntryInterface;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The KettleStepAnalyzerProvider maintains a collection of analyzer objects capable of analyzing various PDI step
 */
public class JobEntryAnalyzerProvider extends BaseKettleMetaverseComponent implements IJobEntryAnalyzerProvider {

  /**
   * The set of step analyzers.
   */
  protected List<IJobEntryAnalyzer> jobEntryAnalyzers = new ArrayList<IJobEntryAnalyzer>();

  /**
   * The analyzer type map associates step meta classes with analyzers for those classes
   */
  protected Map<Class<? extends JobEntryInterface>, Set<IJobEntryAnalyzer>> analyzerTypeMap =
      new HashMap<Class<? extends JobEntryInterface>, Set<IJobEntryAnalyzer>>();

  /**
   * Returns all registered step analyzers
   *
   * @return a List of step analyzers
   */
  @Override
  public List<IJobEntryAnalyzer> getAnalyzers() {
    return jobEntryAnalyzers;
  }

  /**
   * Returns the set of analyzers for step with the specified classes
   *
   * @param types a set of classes corresponding to step for which to retrieve the analyzers
   * @return a set of analyzers that can process the specified step
   */
  @Override public List<IJobEntryAnalyzer> getAnalyzers( Collection<Class<?>> types ) {
    List<IJobEntryAnalyzer> stepAnalyzers = getAnalyzers();
    if ( types != null ) {
      final Set<IJobEntryAnalyzer> specificStepAnalyzers = new HashSet<IJobEntryAnalyzer>();
      for ( Class<?> clazz : types ) {
        if ( analyzerTypeMap.containsKey( clazz ) ) {
          specificStepAnalyzers.addAll( analyzerTypeMap.get( clazz ) );
        }
      }
      stepAnalyzers = new ArrayList<IJobEntryAnalyzer>( specificStepAnalyzers );
    }
    return stepAnalyzers;
  }

  /**
   * Sets the collection of step analyzers used to analyze PDI step
   *
   * @param analyzers
   */
  public void setJobEntryAnalyzers( List<IJobEntryAnalyzer> analyzers ) {
    jobEntryAnalyzers = analyzers;
    loadAnalyzerTypeMap();
  }

  /**
   * Loads up a Map of document types to supporting IJobEntryAnalyzer(s)
   */
  protected void loadAnalyzerTypeMap() {
    analyzerTypeMap = new HashMap<Class<? extends JobEntryInterface>, Set<IJobEntryAnalyzer>>();
    if ( jobEntryAnalyzers != null ) {
      for ( IJobEntryAnalyzer analyzer : jobEntryAnalyzers ) {
        addAnalyzer( analyzer );
      }
    }
  }

  @Override
  public void addAnalyzer( IJobEntryAnalyzer analyzer ) {
    if ( !jobEntryAnalyzers.contains( analyzer ) ) {
      jobEntryAnalyzers.add( analyzer );
    }
    Set<Class<? extends JobEntryInterface>> types = analyzer.getSupportedEntries();
    analyzer.setMetaverseBuilder( metaverseBuilder );
    if ( types != null ) {
      for ( Class<? extends JobEntryInterface> type : types ) {
        Set<IJobEntryAnalyzer> analyzerSet = null;
        if ( analyzerTypeMap.containsKey( type ) ) {
          // we already have someone that handles this type, add to the Set
          analyzerSet = analyzerTypeMap.get( type );
        } else {
          // no one else (yet) handles this type, add it in
          analyzerSet = new HashSet<IJobEntryAnalyzer>();
        }
        analyzerSet.add( analyzer );
        analyzerTypeMap.put( type, analyzerSet );
      }
    }
  }

  @Override
  public void removeAnalyzer( IJobEntryAnalyzer analyzer ) {
    if ( jobEntryAnalyzers.contains( analyzer ) ) {
      jobEntryAnalyzers.remove( analyzer );
    }
    Set<Class<? extends JobEntryInterface>> types = analyzer.getSupportedEntries();
    if ( types != null ) {
      for ( Class<? extends JobEntryInterface> type : types ) {
        Set<IJobEntryAnalyzer> analyzerSet = null;
        if ( analyzerTypeMap.containsKey( type ) ) {
          // we have someone that handles this type, remove it from the set
          analyzerSet = analyzerTypeMap.get( type );
          analyzerSet.remove( analyzer );
          if ( analyzerSet.size() == 0 ) {
            analyzerTypeMap.remove( type );
          }
        }
      }
    }
  }

}
