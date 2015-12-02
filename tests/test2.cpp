const OpDef LookUp(string op_type_name,
                                Status status) {
//  const OpDef* op_def = nullptr;
//  bool first_call = false;
//  {  // Scope for lock.
//    mutex_lock lock(mu_);
//    first_call = CallDeferred();
//    op_def = gtl::FindWithDefault(registry_, op_type_name, nullptr);
//    // Note: Can't hold mu_ while calling Export() below.
//  }
//  if (first_call) {
//    TF_QCHECK_OK(ValidateKernelRegistrations(this));
//  }
//  if (op_def == nullptr) {
//    status->Update(
//        errors::NotFound("Op type not registered '", op_type_name, "'"));
//    LOG(INFO) << status->ToString();
//    static bool first_unregistered = true;
//    if (first_unregistered) {
//      OpList op_list;
//      Export(true, &op_list);
//      LOG(INFO) << "All registered Ops:";
//      for (const auto& op : op_list.op()) {
//        LOG(INFO) << SummarizeOpDef(op);
//      }
//      first_unregistered = false;
//    }
//  }
//  return op_def;
}