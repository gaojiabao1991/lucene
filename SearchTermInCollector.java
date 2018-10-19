package com.zhipin.search.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sheeva on 2018/1/2.
 */
public class LuceneIndexPrinter {
    @Test
    public void test() throws IOException {
        RAMDirectory dir = new RAMDirectory();

        index(dir);

        IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(dir));
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        builder.add(new TermQuery(new Term("text", "aa")), BooleanClause.Occur.SHOULD);
        builder.add(new TermQuery(new Term("text", "cc")), BooleanClause.Occur.SHOULD);
        searcher.search(builder.build(), new TestCollector());
    }

    private static class TestCollector implements Collector {
        private LeafReaderContext context;

        @Override
        public LeafCollector getLeafCollector(LeafReaderContext context) throws IOException {
            this.context = context;

            return new LeafCollector() {
                @Override
                public void setScorer(Scorer scorer) throws IOException {
                }

                @Override
                public void collect(int doc) throws IOException {
                    List<String> matches = new ArrayList<>(2);

                    LeafReader reader = context.reader();
                    for (String term : new String[]{"aa", "cc"}) {
                        PostingsEnum postings = reader.postings(new Term("text", term));
                        if (postings != null) {
                            if (postings.advance(doc) == doc) {
                                matches.add(term);
                            }
                        }
                    }
                    System.out.println(String.format("doc: %s, match terms: %s", doc, String.join(",", matches)));
                }
            };
        }

        @Override
        public boolean needsScores() {
            return false;
        }
    }

    private void index(RAMDirectory dir) throws IOException {
        IndexWriterConfig conf = new IndexWriterConfig();
        IndexWriter writer = new IndexWriter(dir, conf);

        Document doc = new Document();
        doc.add(new TextField("text", "aa", Field.Store.NO));
        doc.add(new TextField("text", "bb", Field.Store.NO));
        writer.addDocument(doc);

        doc = new Document();
        doc.add(new TextField("text", "cc", Field.Store.NO));
        doc.add(new TextField("text", "dd", Field.Store.NO));
        writer.addDocument(doc);

        writer.commit();
    }
}
