package org.restlet.example.book.restlet.ch09.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ResourcePrototype;

public class Mailboxes_Images_default_InlineClientBundleGenerator implements org.restlet.example.book.restlet.ch09.client.Mailboxes.Images {
  private static Mailboxes_Images_default_InlineClientBundleGenerator _instance0 = new Mailboxes_Images_default_InlineClientBundleGenerator();
  private void draftsInitializer() {
    drafts = new com.google.gwt.resources.client.impl.ImageResourcePrototype(
      "drafts",
      com.google.gwt.safehtml.shared.UriUtils.fromTrustedString(externalImage),
      0, 0, 16, 16, false, false
    );
  }
  private static class draftsInitializer {
    static {
      _instance0.draftsInitializer();
    }
    static com.google.gwt.resources.client.ImageResource get() {
      return drafts;
    }
  }
  public com.google.gwt.resources.client.ImageResource drafts() {
    return draftsInitializer.get();
  }
  private void homeInitializer() {
    home = new com.google.gwt.resources.client.impl.ImageResourcePrototype(
      "home",
      com.google.gwt.safehtml.shared.UriUtils.fromTrustedString(externalImage0),
      0, 0, 16, 16, false, false
    );
  }
  private static class homeInitializer {
    static {
      _instance0.homeInitializer();
    }
    static com.google.gwt.resources.client.ImageResource get() {
      return home;
    }
  }
  public com.google.gwt.resources.client.ImageResource home() {
    return homeInitializer.get();
  }
  private void inboxInitializer() {
    inbox = new com.google.gwt.resources.client.impl.ImageResourcePrototype(
      "inbox",
      com.google.gwt.safehtml.shared.UriUtils.fromTrustedString(externalImage1),
      0, 0, 16, 16, false, false
    );
  }
  private static class inboxInitializer {
    static {
      _instance0.inboxInitializer();
    }
    static com.google.gwt.resources.client.ImageResource get() {
      return inbox;
    }
  }
  public com.google.gwt.resources.client.ImageResource inbox() {
    return inboxInitializer.get();
  }
  private void sentInitializer() {
    sent = new com.google.gwt.resources.client.impl.ImageResourcePrototype(
      "sent",
      com.google.gwt.safehtml.shared.UriUtils.fromTrustedString(externalImage2),
      0, 0, 16, 16, false, false
    );
  }
  private static class sentInitializer {
    static {
      _instance0.sentInitializer();
    }
    static com.google.gwt.resources.client.ImageResource get() {
      return sent;
    }
  }
  public com.google.gwt.resources.client.ImageResource sent() {
    return sentInitializer.get();
  }
  private void templatesInitializer() {
    templates = new com.google.gwt.resources.client.impl.ImageResourcePrototype(
      "templates",
      com.google.gwt.safehtml.shared.UriUtils.fromTrustedString(externalImage3),
      0, 0, 16, 16, false, false
    );
  }
  private static class templatesInitializer {
    static {
      _instance0.templatesInitializer();
    }
    static com.google.gwt.resources.client.ImageResource get() {
      return templates;
    }
  }
  public com.google.gwt.resources.client.ImageResource templates() {
    return templatesInitializer.get();
  }
  private void trashInitializer() {
    trash = new com.google.gwt.resources.client.impl.ImageResourcePrototype(
      "trash",
      com.google.gwt.safehtml.shared.UriUtils.fromTrustedString(externalImage4),
      0, 0, 16, 16, false, false
    );
  }
  private static class trashInitializer {
    static {
      _instance0.trashInitializer();
    }
    static com.google.gwt.resources.client.ImageResource get() {
      return trash;
    }
  }
  public com.google.gwt.resources.client.ImageResource trash() {
    return trashInitializer.get();
  }
  private void treeClosedInitializer() {
    treeClosed = new com.google.gwt.resources.client.impl.ImageResourcePrototype(
      "treeClosed",
      com.google.gwt.safehtml.shared.UriUtils.fromTrustedString(externalImage5),
      0, 0, 16, 16, false, false
    );
  }
  private static class treeClosedInitializer {
    static {
      _instance0.treeClosedInitializer();
    }
    static com.google.gwt.resources.client.ImageResource get() {
      return treeClosed;
    }
  }
  public com.google.gwt.resources.client.ImageResource treeClosed() {
    return treeClosedInitializer.get();
  }
  private void treeLeafInitializer() {
    treeLeaf = new com.google.gwt.resources.client.impl.ImageResourcePrototype(
      "treeLeaf",
      com.google.gwt.safehtml.shared.UriUtils.fromTrustedString(externalImage6),
      0, 0, 1, 1, false, false
    );
  }
  private static class treeLeafInitializer {
    static {
      _instance0.treeLeafInitializer();
    }
    static com.google.gwt.resources.client.ImageResource get() {
      return treeLeaf;
    }
  }
  public com.google.gwt.resources.client.ImageResource treeLeaf() {
    return treeLeafInitializer.get();
  }
  private void treeOpenInitializer() {
    treeOpen = new com.google.gwt.resources.client.impl.ImageResourcePrototype(
      "treeOpen",
      com.google.gwt.safehtml.shared.UriUtils.fromTrustedString(externalImage7),
      0, 0, 16, 16, false, false
    );
  }
  private static class treeOpenInitializer {
    static {
      _instance0.treeOpenInitializer();
    }
    static com.google.gwt.resources.client.ImageResource get() {
      return treeOpen;
    }
  }
  public com.google.gwt.resources.client.ImageResource treeOpen() {
    return treeOpenInitializer.get();
  }
  private static java.util.HashMap<java.lang.String, com.google.gwt.resources.client.ResourcePrototype> resourceMap;
  private static final java.lang.String externalImage = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAABW0lEQVR42mNgIBHsDQzMOJSdfWOxh0ekDAMDJ0maO3x9ta92dj78tHXr/wMhIVcn2tmlEa05MTpaq3Pi5LsXd+/+/3/y5P+vExL+b3JxeUuU5jXuEtr1rZ0Prz5+8H/BggX/10RE/F9lYfFyo7OzB1Gaz7dm3rl7qfn/lAkV/6fPXvTfyd395WpzczeCmtc5iOuANP95VvX//w6G/4eLWP+72tu/cHJ1JU7zycb0+yDN/87b/n84lfn/TAeG1652duRrnuXI4E6W5oX+Iv+J0rzJXU6bIs0XJgID63wA6ZpX1OhobpsQfOT/87n//29n/v+8joF4zSCwvt+8/+zy0J8/zrT/f7DI7n+/DcMLojWDgIe9wMu5jXb/t012/1mXrHa1z44BPaoY8RrAxc1w39KE+3thlNwGA1Xe1VDhbCgdCKVtoLQulFaCG8DDwyBmZ8mtmx8ta0pq9gYAez/LlWKmUkUAAAAASUVORK5CYII=";
  private static final java.lang.String externalImage0 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAB9ElEQVR42qWRUUtaYRjHD9va6GofYHfW7CjIqOPYCJYUQQhBMAjznDQ7KpvQXW3fQMsaG7E1subgGBaZ5MpSGbkUZy7M6qIYXXbZB9jVLv75vOVhB6sN9sIP3vP8n98fXg7H3XCaWwRfE28e4f7l7HEcatD3Q14Qm3kBBN3/WpCvijWaWtq6+m3S70jkEwi60+zGgrWqSMzea4S19/mv9fU4yuUCg+400/GC6dqCz1V5+k4Dup5asLSk4OCghNPTE8bh4Q8sxxbQ/qz7TK9/9ODKAv+t27CYzAjNfUCptI10+gucThEezzC2tjbYjLLWxx0/dTrhvkY2Go13O3QGTAT9yOUyOD7eQ19fLzo7LbBaeyDLQ2xGWXAyAIPpyTY5aoHeYF4cHRvD5uYqKpXvODoqM+FPaFapFJFKrYJ2ybn417wwLru9iMejKBaz2N8vVt+/cyWU0Q7tkkMuK1GUuct3fsPubo7hfj8Mz0cZ3pAbL+a96px2aJcc9QkrKwvI5zMoFL6quN45kS6kGN5ZWZPRLjlqQSwWQTa7ocHxRoLv1Uv4Xvsgz7jqcnLUgmg0jEwmoUEK2uGYEuF8OwjXtKMuJ0ctUJQQksmYBjEwAGliAIOsRKrLyVELwuEZJBKLGux+G+wBG8RxG6RJe11OTs1P/g/n6unbjnlbJo8AAAAASUVORK5CYII=";
  private static final java.lang.String externalImage1 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAABVUlEQVR42p2Q30rCUBjADwQ9QNB79CRBL9FNddMDdFXdBT1BJUbKojALGWdzuoXDLb1xEwz/xJx/MgJnGDZzfu3ryrGxbB/8bs75/Q4fh5CFoZQ/4DgewkCHBA2ldENRVPhrVPUJ0PU9wPP8aa/Xh7wowuHRMezs7nnAs1wuD+i4W5x4YoZhViVJenccBy5icTg7jwWCd+hI0uPAbVYW1s9uNZtNqFQ0iF9ehYIOupQKmwvrCynbtiGVvockcx0KOuhms8LNb5zJZNY1TXfaZgdu79JLYZgm6HrVZll2DT9v37IskBUFHlh2KQrFImDDcdw2EUWpOp1OgQq5f4GN25ZJp9OF+Rw8dPuvkC/IHvqDN5+HLWm3TZjNHB9yqewhyMGWGIbhrvPto6RpHoIcbEmr9QKTyVcksCX1egPG489IYEtqtWcYjT4igS0ZDi1IJJKRwPYHLLEg8Bujbf4AAAAASUVORK5CYII=";
  private static final java.lang.String externalImage2 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAA1UlEQVR42sWTLQuFQBBF55+bLP4Dk8FmsSiCYLBY7DYxCIJiERFExPveXXii8j7lgQsHZmfuGdiwIpefKIq0OziJJmEYIk1TLMvyE3Toiu/7aNsWSZJgnuevYJYOXXFdF9M0oa5rxHGs6ncwwyxruuI4DsZxVFRVpd72uB/hjJnHna7Yto1hGFbKskQQBLseYY+zbY+uWJaFvu93FEUBz/PQdZ2CNXvHHF0xTXMNbsnzXImE9bMMXTEMA03TPCXLMsWrOd23Cz7xnwW6rmt3cBLt+s94A8xzoifj/MfyAAAAAElFTkSuQmCC";
  private static final java.lang.String externalImage3 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAABA0lEQVR42q2RW2rCQBiFs5RupDtIV9A+ZxGWPBWShypqbQ2JqSEjgYhgCNFa6SW06IPgOlyA+j7mJMOMFwgZ8MBh4Ge+Lz8ZRWExm9aN+WKlZtuipcWd7K5yno5LNtvdnlap5QWbE7jedZKf/yVdrNaV+vW3oGC44PnNobIBwwWNrpsPNU27KHL7eFoEDBe0bK/0a6qdgTV2MkHb8YTg9Z2UbnBnF1VZkY7rC0H2V0s3uJ8V4MNMCMBwQY+E+VDX9YsiAI+L9AahEPSDkfQrgOECMozyoWEYlYr44VgIglEsvQEYLhhGU+kNwHBB/PEtvQGY6wmSefo0mae0apPPXwoG7AFcaNoLS5mKdQAAAABJRU5ErkJggg==";
  private static final java.lang.String externalImage4 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAB20lEQVR42pWSTUsbURiF780kQsGtuOi24j8Q0Qjxq2o04saVoKJUCuKmNIJBqUZxoa1U8QPEj2LUATGBIrXCpESDEw0dTSkoailojajYX+DC47wzyUVBQnzh4b4Hzjkz3BnGEqMoil1RfmwqShCpIY9iZw+nSPVUN6mfcHl9nRYN6hAoIwpytzqCPzUN+7FfWPAtpWQvFoOm7SFnsz1kpkP1mRnf628Pj46gRqP4+m09JeQhL2Uoy5jf1cpXXfgXjyOiN4d3oykhz8HZH1CGrbraGF9yajqIX12mfQfHF6fgy05QlvGZipi0XAM+W5E+c5WQZCOjMTZe5rZ4C2AdKX4WlGGTpe8YGyl5afnouLMF6pDE/XsOD/VTUIayxofIHnbeWH1O2FZqEThXQUMnaYLGeyALbfVVI2u46r/4DwpGm8PSWCkCZ2Y4OaRtizUGNMmdvIWfW7ZFQeO0Z8IyUAjbbKWB8cT9RaFpNwoS2jJgR8N055Qo6Jcn3vDufFgnyw1okrv/b/jRW5HmPfn4II++FQXza7KDe/L023UYeHe+mCUJ7T/ZMsP6SZq8lBEFGxuhV1m9ZZD67ZAGi57kfXDc3HVPdt9rUEYURCKRF+6FIfCuvLQgL2Uoew8iFUt4RsLHYAAAAABJRU5ErkJggg==";
  private static final java.lang.String externalImage5 = "data:image/gif;base64,R0lGODlhEAAQAIQaAFhorldnrquz1mFxsvz9/vr6/M3Q2ZGbw5mixvb3+Gp5t2Nys77F4GRzs9ze4mt6uGV1s8/R2VZnrl5usFdortPV2/P09+3u8eXm6lZnrf///wAAzP///////////////yH5BAEAAB8ALAAAAAAQABAAAAVE4CeOZGmeaKquo5K974MuTKHdhDCcgOVvvoTkRLkYN8bL0ETBbJ5PTIaIqW6q0lPAYcVOTRNEpEI2HCYoCOzVYLnf7hAAOw==";
  private static final java.lang.String externalImage6 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAAC0lEQVR42mNgAAIAAAUAAen63NgAAAAASUVORK5CYII=";
  private static final java.lang.String externalImage7 = "data:image/gif;base64,R0lGODlhEAAQAIQaAFhorldnrquz1mFxsvz9/vr6/M3Q2ZGbw5mixvb3+Gp5t2Nys77F4GRzs9ze4mt6uGV1s8/R2VZnrl5usFdortPV2/P09+3u8eXm6lZnrf///wAAzP///////////////yH5BAEAAB8ALAAAAAAQABAAAAVD4CeOZGmeaKquo5K974MuTKHdhDCcgOVfvoTkRLkYj5ehiYLZOJ2YDBFDvVCjp4CjepWaJohIZWw4TFAQ2KvBarvbIQA7";
  private static com.google.gwt.resources.client.ImageResource drafts;
  private static com.google.gwt.resources.client.ImageResource home;
  private static com.google.gwt.resources.client.ImageResource inbox;
  private static com.google.gwt.resources.client.ImageResource sent;
  private static com.google.gwt.resources.client.ImageResource templates;
  private static com.google.gwt.resources.client.ImageResource trash;
  private static com.google.gwt.resources.client.ImageResource treeClosed;
  private static com.google.gwt.resources.client.ImageResource treeLeaf;
  private static com.google.gwt.resources.client.ImageResource treeOpen;
  
  public ResourcePrototype[] getResources() {
    return new ResourcePrototype[] {
      drafts(), 
      home(), 
      inbox(), 
      sent(), 
      templates(), 
      trash(), 
      treeClosed(), 
      treeLeaf(), 
      treeOpen(), 
    };
  }
  public ResourcePrototype getResource(String name) {
    if (GWT.isScript()) {
      return getResourceNative(name);
    } else {
      if (resourceMap == null) {
        resourceMap = new java.util.HashMap<java.lang.String, com.google.gwt.resources.client.ResourcePrototype>();
        resourceMap.put("drafts", drafts());
        resourceMap.put("home", home());
        resourceMap.put("inbox", inbox());
        resourceMap.put("sent", sent());
        resourceMap.put("templates", templates());
        resourceMap.put("trash", trash());
        resourceMap.put("treeClosed", treeClosed());
        resourceMap.put("treeLeaf", treeLeaf());
        resourceMap.put("treeOpen", treeOpen());
      }
      return resourceMap.get(name);
    }
  }
  private native ResourcePrototype getResourceNative(String name) /*-{
    switch (name) {
      case 'drafts': return this.@org.restlet.example.book.restlet.ch09.client.Mailboxes.Images::drafts()();
      case 'home': return this.@org.restlet.example.book.restlet.ch09.client.Mailboxes.Images::home()();
      case 'inbox': return this.@org.restlet.example.book.restlet.ch09.client.Mailboxes.Images::inbox()();
      case 'sent': return this.@org.restlet.example.book.restlet.ch09.client.Mailboxes.Images::sent()();
      case 'templates': return this.@org.restlet.example.book.restlet.ch09.client.Mailboxes.Images::templates()();
      case 'trash': return this.@org.restlet.example.book.restlet.ch09.client.Mailboxes.Images::trash()();
      case 'treeClosed': return this.@com.google.gwt.user.client.ui.Tree.Resources::treeClosed()();
      case 'treeLeaf': return this.@org.restlet.example.book.restlet.ch09.client.Mailboxes.Images::treeLeaf()();
      case 'treeOpen': return this.@com.google.gwt.user.client.ui.Tree.Resources::treeOpen()();
    }
    return null;
  }-*/;
}
