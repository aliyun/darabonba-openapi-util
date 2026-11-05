find_package(darabonba_util QUIET)

if(darabonba_util_FOUND)
  message(STATUS "Found darabonba_util version ${darabonba_util_VERSION}")
else()
  message(STATUS "darabonba_util could not be located, Building darabonba_util instead.")
  include(FetchContent)

  FetchContent_Declare(
    _darabonba_util
    GIT_REPOSITORY https://github.com/alibabacloud-sdk-cpp/dara-util.git
    GIT_TAG master
    CMAKE_ARGS
      -DENABLE_UNIT_TESTS:BOOL="0")

  FetchContent_GetProperties(_darabonba_util)
  if(NOT _darabonba_util_POPULATED)
    FetchContent_Populate(_darabonba_util)
    message(STATUS "Added darabonba_util source : ${_darabonba_util_SOURCE_DIR}")
    message(STATUS "Added darabonba_util binary : ${_darabonba_util_BINARY_DIR}")
    set(_OPENAPI_ENABLE_UNIT_TESTS ${ENABLE_UNIT_TESTS})
    set(ENABLE_UNIT_TESTS OFF)
    if(POLICY CMP0077)
      set(CMAKE_POLICY_DEFAULT_CMP0077 NEW)
    else()
      set(ENABLE_UNIT_TESTS OFF CACHE BOOL "Enable unit tests" FORCE)
    endif()
    add_subdirectory(
      ${_darabonba_util_SOURCE_DIR}
      ${_darabonba_util_BINARY_DIR}
    )
    set(ENABLE_UNIT_TESTS ${_OPENAPI_ENABLE_UNIT_TESTS})
    if(NOT POLICY CMP0077)
      set(ENABLE_UNIT_TESTS ${_OPENAPI_ENABLE_UNIT_TESTS} CACHE BOOL "Enable unit tests" FORCE)
    endif()
    find_package(jsoncpp CONFIG REQUIRED)
    set_property(TARGET JsonCpp::JsonCpp PROPERTY IMPORTED_GLOBAL TRUE)
    set_property(TARGET darabonba_util APPEND PROPERTY LINK_LIBRARIES JsonCpp::JsonCpp)
  endif()
  target_include_directories(alibabacloud_open_api_util PRIVATE "${_darabonba_util_SOURCE_DIR}/include")
  if(ENABLE_UNIT_TESTS)
    target_include_directories(tests_alibabacloud_open_api_util PRIVATE "${_darabonba_util_SOURCE_DIR}/include")
  endif()
endif()
