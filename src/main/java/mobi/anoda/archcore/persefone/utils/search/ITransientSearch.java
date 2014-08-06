package mobi.anoda.archcore.persefone.utils.search;

interface ITransientSearch {

    QueryProxy obtainQueryProcessor();

    void initQueryProxy(CharSequence query);
}