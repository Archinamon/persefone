package mobi.anoda.archinamon.kernel.persefone.utils.search;

interface ITransientSearch {

    QueryProxy obtainQueryProcessor();

    void initQueryProxy(CharSequence query);
}