find_package(darabonba_core QUIET)

if(darabonba_core_FOUND)
  message(STATUS "Found darabonba_core version ${darabonba_core_VERSION}")
else()
  message(STATUS "darabonba_core could not be located, Building darabonba_core instead.")
  include(FetchContent)

  FetchContent_Declare(
    _darabonba_core
    GIT_REPOSITORY https://github.com/aliyun/tea-cpp.git
    GIT_TAG 4cdc0046a3c105b23c7065a469aeab51d714c8ae
    CMAKE_ARGS
      -DENABLE_UNIT_TESTS:BOOL="0")

  FetchContent_GetProperties(_darabonba_core)
  if(NOT _darabonba_core_POPULATED)
    FetchContent_Populate(_darabonba_core)
    message(STATUS "Added darabonba_core source : ${_darabonba_core_SOURCE_DIR}")
    message(STATUS "Added darabonba_core binary : ${_darabonba_core_BINARY_DIR}")
    set(_OPENAPI_ENABLE_UNIT_TESTS ${ENABLE_UNIT_TESTS})
    set(ENABLE_UNIT_TESTS OFF)
    set(CMAKE_POLICY_DEFAULT_CMP0077 NEW)
    add_subdirectory(
      ${_darabonba_core_SOURCE_DIR}
      ${_darabonba_core_BINARY_DIR}
    )
    set(ENABLE_UNIT_TESTS ${_OPENAPI_ENABLE_UNIT_TESTS})
  endif()
  target_include_directories(alibabacloud_open_api_util PRIVATE "${_darabonba_core_SOURCE_DIR}/include")
  if(ENABLE_UNIT_TESTS)
    target_include_directories(tests_alibabacloud_open_api_util PRIVATE "${_darabonba_core_SOURCE_DIR}/include")
  endif()
endif()
