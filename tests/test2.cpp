void process_data(string str)
{
    vector<string> arr;
    boost::split(arr, str, boost::is_any_of(" \n"));
    do_some_operation(arr);
}

int main()
{
    int read_bytes = 45 * 1024 *1024;
    char fname = "input.txt";
    ifstream fin(fname, ios::in);
    char memblock;

    while(!fin.eof())
    {
        fin.read(memblock, read_bytes);
        string str(memblock);
        process_data(str);
        delete [] memblock;
    }
    return 0;
}